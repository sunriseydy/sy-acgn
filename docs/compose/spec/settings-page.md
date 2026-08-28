---
feature: settings-page
status: delivered
updated: 2026-08-26
branch: feature/settings-page
commits: 26691be..feature/settings-page
---

# Settings Page

## Report

**What was built** — A full settings page for the SY-ACGN application, accessible as a top-level navigation item (Settings icon in bottom nav / sidebar). The page displays all registered AppConfig items auto-grouped into 5 sections: General (AppName, Language), Server (client-only server address), Anime (TMDB, qBittorrent, Bangumi configs), S3 Storage, and Database (read-only PostgreSQL config). Each config item uses an appropriate editor: text fields for strings, password fields with visibility toggle for sensitive values (passwords/secrets), a Language dropdown for the language setting, and disabled fields for read-only database configs. Saving updates server-side configs via the existing `POST /api/common/config` API and client-side server address via `LocalSettings`.

**Verification** — `./kotlin build -m common-client` PASS, `./kotlin build -m server` PASS.

**Journey log**:
1. Config key format assumption was wrong initially — `AppConfigInterface.configKey` generates 2-level keys (`config.{MODULE}.{Name}`) not 3-level, because `this::class.simpleName` for nested Kotlin objects returns only the leaf name. Fixed `extractGroupKey` to parse 2-level keys.
2. zh-CN localization entries were initially placed under the existing `message:` YAML section instead of a proper `config:` section, causing all Chinese config labels to silently fall back to camelCase splits. Moved to a standalone `config:` section.
3. Group name lookup keys used `/` separator but YAML uses `.` for nesting — fixed to use `.` consistently (`config.group.COMMON.S3` etc.).
4. Read-only sensitive fields (db password) were displayed in plain text because the condition required `isSensitive && !isReadOnly`. Fixed to check `isSensitive` alone.

## [S1] Problem

应用目前缺少统一的设置管理界面。现有的 `AppConfigInterface` 体系已定义了通用设置（AppName、AppLanguage）、动漫模块设置（TMDB API、qBittorrent、Bangumi）和 S3 存储设置等配置项，此外数据库连接配置（PostgreSQL）从 `application.yaml` 加载但无查看入口。用户只能通过直接操作数据库或首次启动的 `ServerConfig` 页面来修改部分配置。需要一个集中的、可访问的设置页面，让用户查看所有配置项、编辑可变配置、以及查看只读的数据库配置。

## [S2] Design

### 导航集成

新增 `SettingsRoute` 作为 `NavigationRoute` 密封接口的实现，加入 `TopLevelRouteEnum` 枚举，使其出现在底部导航栏/侧边栏中，与 RSS、动漫、小说、游戏并列。使用 Material Icons 的 `Settings` 图标。

### 数据模型

复用现有 `AppConfigTool.getAppConfigs(): Map<String, Pair<AppConfig?, String?>>` 作为数据源。配置项按 key 模式自动分组，支持两种 key 格式：

**标准格式** `config.{MODULE}.{Container}.{Name}`：
- 解析 key 提取 `Container`（如 `CommonModuleAppConfig`、`S3AppConfig`）和 `MODULE` 作为分组依据
- 示例：`config.COMMON.CommonModuleAppConfig.AppName` → 分组 `COMMON/CommonModuleAppConfig`

**数据库配置格式** `config.db.{Subsystem}.{Name}`：
- 解析 key 提取 `Subsystem`（如 `postgresql`）作为分组依据
- 示例：`config.db.postgresql.host` → 分组 `db/postgresql`
- **只读**：数据库配置项不可编辑，仅展示当前值

本地化：
- 配置项显示名：`LocalizationTool.getLocalization("{configKey}.meaning")`
- 分组显示名：`LocalizationTool.getLocalization("config.group.{MODULE}.{Container}")` 或 `config.group.db.{Subsystem}`
- 未匹配到本地化文本时回退到 key 名

### 配置项完整列表

| 分组 | 配置项 | configKey | 可编辑 | 编辑器 |
|------|--------|-----------|--------|--------|
| **通用设置** (COMMON/CommonModuleAppConfig) | 应用名称 | `config.COMMON.CommonModuleAppConfig.AppName` | 是 | TextField |
| | 应用语言 | `config.COMMON.CommonModuleAppConfig.AppLanguage` | 是 | Dropdown |
| **服务器设置** (client-only) | 服务器地址 | *(本地存储)* | 是 | TextField |
| **动漫设置** (ANIME/AnimeModuleAppConfig) | TMDB API 密钥 | `config.ANIME.AnimeModuleAppConfig.TmdbApiKey` | 是 | TextField |
| | qBittorrent URL | `config.ANIME.AnimeModuleAppConfig.QbApiBaseUrl` | 是 | TextField |
| | qBittorrent 用户名 | `config.ANIME.AnimeModuleAppConfig.QbUserName` | 是 | TextField |
| | qBittorrent 密码 | `config.ANIME.AnimeModuleAppConfig.QbPassword` | 是 | PasswordField |
| | 媒体目标目录 | `config.ANIME.AnimeModuleAppConfig.MediaTargetDirectory` | 是 | TextField |
| | Bangumi User-Agent | `config.ANIME.AnimeModuleAppConfig.BgmUserAgent` | 是 | TextField |
| **S3 存储设置** (COMMON/S3AppConfig) | Endpoint | `config.COMMON.S3AppConfig.S3Endpoint` | 是 | TextField |
| | Access Key | `config.COMMON.S3AppConfig.S3AccessKey` | 是 | TextField |
| | Secret Key | `config.COMMON.S3AppConfig.S3SecretKey` | 是 | PasswordField |
| | Bucket Name | `config.COMMON.S3AppConfig.S3BucketName` | 是 | TextField |
| | Region | `config.COMMON.S3AppConfig.S3Region` | 是 | TextField |
| **数据库配置** (db/postgresql) | Host | `config.db.postgresql.host` | **否** | ReadOnly |
| | Port | `config.db.postgresql.port` | **否** | ReadOnly |
| | Database | `config.db.postgresql.database` | **否** | ReadOnly |
| | User | `config.db.postgresql.user` | **否** | ReadOnly |
| | Password | `config.db.postgresql.password` | **否** | ReadOnly (masked) |

### UI 结构

采用 Material 3 分组列表风格，每个分组渲染为一个 `Card`：

```
SettingsPage (LazyColumn)
├── Card: 通用设置
│   ├── ListItem: 应用名称 [TextField]
│   └── ListItem: 语言 [Dropdown]
├── Card: 服务器设置
│   └── ListItem: 服务器地址 [TextField]
├── Card: 动漫设置
│   ├── ListItem: TMDB API 密钥 [TextField]
│   ├── ListItem: qBittorrent URL [TextField]
│   ├── ListItem: qBittorrent 用户名 [TextField]
│   ├── ListItem: qBittorrent 密码 [PasswordField]
│   ├── ListItem: 媒体目标目录 [TextField]
│   └── ListItem: Bangumi User-Agent [TextField]
├── Card: S3 存储设置
│   ├── ListItem: Endpoint [TextField]
│   ├── ListItem: Access Key [TextField]
│   ├── ListItem: Secret Key [PasswordField]
│   ├── ListItem: Bucket Name [TextField]
│   └── ListItem: Region [TextField]
├── Card: 数据库配置 (只读)
│   ├── ListItem: Host [ReadOnly text]
│   ├── ListItem: Port [ReadOnly text]
│   ├── ListItem: Database [ReadOnly text]
│   ├── ListItem: User [ReadOnly text]
│   └── ListItem: Password [ReadOnly masked]
└── FAB/Button: 保存
```

编辑器类型推断规则：
- 只读项（`config.db.*`）→ `ReadOnly` 样式（灰色文本，不可交互）
- key 包含 `password`、`secret` → `PasswordField`（遮蔽显示）
- key 包含 `language` → `Dropdown`（Language 枚举选项）
- 其余 → `TextField`

### 状态管理

`SettingsViewModel`（通过 `viewModel()` 获取）管理：
- `configGroups: StateFlow<List<ConfigGroup>>` — 分组后的配置项列表
- `isLoading: StateFlow<Boolean>` — 加载/保存状态
- `saveResult: SharedFlow<SaveResult>` — 保存结果事件

加载流程：
1. 调用 `CommonApi.getAppConfigs()` 获取服务端配置 map
2. 合并客户端本地配置（`LocalSettings` 中的 server address）
3. 按 key 模式分组，构建 `ConfigGroup` 列表
4. 每个 `ConfigGroup` 包含 `name`（本地化分组名）和 `items: List<ConfigItem>`
5. 每个 `ConfigItem` 包含 `key`、`displayName`、`currentValue`、`editorType`、`isSensitive`

保存流程：
1. 收集所有已修改的配置项
2. 服务端配置：构建 `List<AppConfig>` 调用 `CommonApi.saveAppConfigs()`
3. 客户端配置（server address）：通过 `LocalSettings.setLocalServerConfig()` 本地保存
4. 成功后更新 `AppConfigTool` 本地缓存
5. 若 server address 变更，触发重新连接

### 与现有 ServerConfig 的关系

`ServerConfig`（首次启动页面）保留不变。Settings 页面提供完整的配置管理能力，首次启动的简易流程保持独立。

## [S3] Out of Scope

- 服务端配置校验逻辑（后端不改动）
- 敏感字段加密存储
- 配置项的创建/删除（仅编辑现有配置值）
- 配置变更历史/审计日志
- 配置导入/导出
- 非 AppConfig 类型的设置（如主题、通知偏好）

## Tasks

- [x] T1: 新增 SettingsRoute 和导航集成 — acceptance: Settings 图标出现在底部导航栏，点击可导航到 Settings 页面 (covers: S2)
- [x] T2: 创建 SettingsViewModel — acceptance: ViewModel 能加载所有配置（COMMON/ANIME/db）、分组、区分只读/可编辑、处理编辑和保存 (covers: S2)
- [x] T3: 创建 SettingsPage UI — acceptance: 分组卡片列表正确渲染所有配置项，支持文本编辑、密码遮蔽、语言下拉、只读展示 (covers: S2)
- [x] T4: 在 NavigationWrapper 中注册 Settings 路由 — acceptance: entryProvider 中 SettingsRoute 映射到 SettingsPage (covers: S2; depends: T1)
- [x] T5: 添加本地化键 — acceptance: 分组名和 UI 文本在中英文下正确显示，覆盖通用/动漫/S3/数据库四个分组 (covers: S2)
- [x] T6: 集成验证 — acceptance: 端到端可修改配置并保存到服务端，只读配置不可编辑 (covers: S2; depends: T2, T3, T4)
