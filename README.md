# SY ACGN

## 目录

- [项目概述](#项目概述)
  - [快速开始](#快速开始)
- [构建系统：Kotlin Toolchain](#构建系统 Kotlin Toolchain)
  - [常用命令](#常用命令)
  - [模块结构](#模块结构)
- [架构](#架构)
  - [后端（服务器）](#后端服务器)
  - [共享库（Lib）](#共享库lib)
  - [前端（客户端）](#前端客户端)
- [配置](#配置)
  - [环境变量（.env）](#环境变量env)
  - [后端服务配置](#后端服务配置)
  - [配置优先级](#配置优先级)
- [关键模式](#关键模式)
- [测试](#测试)
- [Docker](#docker)
- [架构最佳实践](#架构最佳实践)
- [最近重大改进](#最近重大改进)
- [重要说明](#重要说明)

## 项目概述

SY-ACGN 是一个基于 Kotlin 的 ACGN 管理系统，包含 Ktor 后端服务器、Compose Multiplatform 客户端（桌面端与 WebAssembly Web 端）和共享库模块。项目使用 **Kotlin Toolchain** 作为构建工具。

### 快速开始

```bash
# 运行服务器
./kotlin run -m server

# 运行桌面客户端
./kotlin run -m desktop-client

# 运行 WebAssembly 网页端客户端
./kotlin run -m wasm-client

# 打包服务器
./kotlin build -m server
```

## 构建系统：Kotlin Toolchain

本项目使用 JetBrains Kotlin Toolchain 而非 Gradle。所有构建操作都通过 `./kotlin` 脚本进行。

### 常用命令

```bash
# 运行服务器
./kotlin run -m server

# 运行桌面客户端
./kotlin run -m desktop-client

# 运行 WebAssembly (Wasm) 网页客户端
./kotlin run -m wasm-client

# 编译 WebAssembly (Wasm) 网页客户端
./kotlin build -m wasm-client

# 打包服务器（生成可执行 JAR）
./kotlin package -m server

# 编译服务器
./kotlin build -m server
```

### 模块结构

项目在 `project.yaml` 中定义了多个模块：

```
sy-acgn/
├── server/              # Ktor 后端服务器
│   ├── src/
│   │   ├── base/       # 核心插件和配置
│   │   ├── anime/      # 动漫功能模块
│   │   ├── game/       # 游戏功能模块
│   │   ├── novel/      # 小说功能模块
│   │   ├── common/     # 通用功能模块
│   │   └── Main.kt
│   ├── resources/      # 配置和静态资源
│   └── module.yaml
│
├── lib/                 # 共享 Kotlin 库（多平台：JVM / Wasm-JS）
│   ├── src/
│   │   ├── base/       # 核心接口、Result、枚举
│   │   ├── anime/      # 动漫模块 DTO/枚举/资源
│   │   ├── game/       # 游戏模块 DTO/枚举/资源
│   │   ├── novel/      # 小说模块 DTO/枚举/资源
│   │   ├── common/     # 通用模块 DTO/枚举/资源
│   │   └── tools/      # 跨模块工具类
│   ├── src@jvm/        # JVM 专属实现（如 ReentrantLock）
│   ├── src@wasmJs/     # Wasm-JS 专属实现
│   └── module.yaml
│
├── common-client/       # 共享 Compose UI 组件（多平台：JVM / Wasm-JS）
│   ├── src/
│   │   ├── base/       # 导航、API、通用组件
│   │   ├── anime/      # 动漫页面和组件
│   │   ├── game/       # 游戏页面和 API
│   │   ├── novel/      # 小说页面和 API
│   │   └── common/     # 通用 API 客户端
│   ├── src@jvm/        # JVM 专属存储与缓存实现
│   ├── src@wasmJs/     # Wasm-JS 专属存储与缓存实现
│   ├── composeResources/ # Compose 多平台资源
│   └── module.yaml
│
├── desktop-client/      # 桌面应用入口点（JVM）
│   ├── src/Main.kt
│   └── module.yaml
│
└── wasm-client/         # WebAssembly 网页端入口点（Wasm-JS）
    ├── src/Main.kt
    ├── resources/index.html
    └── module.yaml
```

**依赖关系：**

```
desktop-client    wasm-client
     \                /
      ↓              ↓
       common-client  →  lib
             ↓            ↓
           server    ←────┘
```

每个模块都有一个 `module.yaml` 文件定义其依赖关系。此外，项目根目录包含多个 `*.module-template.yaml` 模板文件用于统一配置：

- `jvm.module-template.yaml` - JVM 平台设置
- `kotlin.module-template.yaml` - Kotlin 编译器设置
- `maven.module-template.yaml` - Maven 仓库配置
- `lib.module-template.yaml` - 库模块公共依赖
- `compose.module-template.yaml` - Compose 相关设置
- `client.module-template.yaml` - 客户端公共依赖

## 架构

### 后端（服务器）

**技术栈：**
- Ktor（CIO 引擎）作为 HTTP 服务器
- Ktor DI（`io.ktor:ktor-server-di`）用于依赖注入
- Exposed 用于 PostgreSQL 数据库访问
- Kotlinx Serialization 用于 JSON 序列化
- Caffeine 缓存
- dotenv-kotlin 加载 `.env` 环境变量
- Kotlin Logging + Logback

**应用入口 (`server/src/Main.kt`)：**
- 使用 `dotenv-kotlin` 加载 `.env` 文件中的环境变量到 System Properties
- 通过 `EngineMain.main(args)` 启动 Ktor CIO 引擎
- `.env` 文件缺失时自动忽略

**核心 Ktor 插件 (`server/src/base/plugins/`)：**
- `Application.kt` - 主入口点，注册所有插件
- `DependencyInjection.kt` - 使用 Ktor DI 注入 Repository、Service 和工具类
- `Databases.kt` - PostgreSQL 连接和表结构迁移
- `AppConfig.kt` - 从文件和数据库加载配置
- `Localization.kt` - 加载后端多语言字符串
- `Routing.kt` - API 路由配置和错误处理
- `Serialization.kt` - JSON 序列化设置
- `HTTP.kt` - HTTP 客户端配置
- `Monitoring.kt` - 日志记录设置

**依赖注入 (`DependencyInjection.kt`)：**
使用 Ktor 内置的 DI 插件注册以下组件：
- Repository
- Service
- 工具类

**模块化架构 (`server/src/`)：**

1. **Base 模块** (`server/src/base/`)
   - `plugins/` - 核心插件（Application、DI、Databases、Routing 等）
   - `config/` - 配置类
     - `PostgresqlConfig.kt` - 数据库配置（使用 `AppConfigInterface` 模式）

2. **Anime 模块** (`server/src/anime/`)
   - `entity/` - 数据表定义及对应 DAO
   - `repository/` - 数据访问层
   - `service/` - 业务逻辑层
   - `routes/` - API 路由
   - `tools/` - 工具类
     - `AnimeCacheTool.kt` - 基于 Caffeine 的动漫数据缓存（24小时过期）
     - `BangumiTool.kt` - Bangumi API 客户端（搜索动漫、获取条目详情）
     - `TmdbTool.kt` - TMDB API 客户端
     - `QbTool.kt` - qBittorrent API 客户端
     - `FileTool.kt` - 文件管理工具（视频/字幕识别、媒体库目录生成）
     - `bangumi/model/` - Bangumi API 数据模型
     - `tmdb/` - TMDB API 完整客户端实现（API、模型、序列化等）
     - `torrent/` - Torrent 解析工具

3. **Game 模块** (`server/src/game/`)
   - `entity/` - 游戏数据表及对应 DAO
   - `repository/` - 数据访问层
   - `service/` - 业务逻辑层
   - `routes/` - API 路由
   - `tools/` - 工具类
     - `GameCacheTool.kt` - 基于 Caffeine 的游戏数据缓存
     - `SteamTool.kt` - Steam API 客户端（获取游戏详情与数据同步）
     - `BangumiGameTool.kt` - Bangumi 游戏数据同步

4. **Novel 模块** (`server/src/novel/`)
   - `entity/` - 小说数据表及对应 DAO
   - `repository/` - 数据访问层
   - `service/` - 业务逻辑层
   - `routes/` - API 路由
   - `tools/` - 工具类
     - `NovelCacheTool.kt` - 基于 Caffeine 的小说数据缓存
     - `BangumiNovelTool.kt` - Bangumi 小说数据搜索与导入工具

5. **Common 模块** (`server/src/common/`)
   - `entity/` - 数据表定义及对应 DAO
   - `repository/` - 数据访问层
   - `service/` - 业务逻辑层
   - `routes/` - API 路由

**数据库模式迁移：**
使用 Exposed 的 `MigrationUtils.statementsRequiredForDatabaseMigration()` 自动迁移表：
- `animeTables()` - 动漫相关表（`AnimeTable`、`AnimeSeasonTable`、`AnimeEpisodeTable`）
- `gameTables()` - 游戏相关表（`GameTable` 等）
- `novelTables()` - 小说相关表（`NovelTable` 等）
- `commonModuleTables()` - 通用模块表（`app_config`、`additional_info`）

**分层架构模式：**
- **接口 + 实现分离**：所有 Repository 和 Service 采用接口定义 + 实现类的模式
- **实现类独立文件**：每个 `*Impl.kt` 文件包含对应接口的具体实现
- **Ktor DI 注入**：通过 `dependencies { provide<Interface> { Impl() } }` 注册，使用 `by application.dependencies` 注入

### 共享库（Lib）

**模块化结构 (`lib/src/`)：**

1. **Base 模块** (`lib/src/base/`)
   - `Result.kt` - 通用结果类型
   - `ApiResources.kt` - API 资源定义
   - `enums/` - 基础枚举类型
   - `exception/` - 异常类型
   - `interfaces/` - 核心接口

2. **Anime 模块** (`lib/src/anime/`)
   - `dto/` - 数据传输对象
   - `config/` - 模块配置
   - `enums/` - 模块枚举
   - `AnimeModuleResources.kt` - 模块 API 资源定义

3. **Game 模块** (`lib/src/game/`)
   - `dto/` - 游戏相关数据传输对象
   - `enums/` - 游戏相关枚举与错误码定义
   - `GameModuleResources.kt` - 游戏模块 API 资源定义

4. **Novel 模块** (`lib/src/novel/`)
   - `dto/` - 小说相关数据传输对象
   - `enums/` - 小说相关枚举定义
   - `NovelModuleResources.kt` - 小说模块 API 资源定义

5. **Common 模块** (`lib/src/common/`)
   - `dto/` - 数据传输对象
   - `config/` - 模块配置
   - `enums/` - 模块枚举
   - `CommonModuleResources.kt` - 模块 API 资源定义

4. **工具类** (`lib/src/tools/`)
   - `LocalizationTool.kt` - 本地化工具
   - `AppConfigTool.kt` - 应用配置工具
   - `HttpClientFactory.kt` - HTTP 客户端工厂

5. **序列化工具** (`lib/src/`)
   - `OffsetDateTimeSerializer.kt` - `OffsetDateTime` 序列化器

**核心设计模式：**

1. **Result 模式** (`lib/src/base/Result.kt`)
    - 通用 `Result<T>` 类，包含 `failed`、`message`、`data` 字段
    - `checkSuccess()` 和 `checkSuccessAndNotNull()` 辅助方法

2. **后端多语言系统** (`lib/src/base/interfaces/KeyInterface.kt`)
    - `Key` 接口：`moduleName`、`key`、`meaning`（自动本地化）
    - `EnumKey`：枚举本地化（键格式：`enum.{MODULE}.{ClassName}.{NAME}`）
    - `Message`：消息本地化（键格式：`message.{LEVEL}.{MODULE}.{NAME}`）
    - `ErrorMessage`：ERROR 级别消息
    - `CommonModule`/`AnimeModule`：模块特定接口

3. **本地化工具** (`lib/src/tools/LocalizationTool.kt`)
    - 单例对象管理语言字符串
    - `putAll()`、`getLocalization()` 方法
    - `i()` 快速本地化访问函数

4. **应用配置工具** (`lib/src/tools/AppConfigTool.kt`)
    - 双层配置：基于文件（默认）+ 基于数据库（覆盖）
    - 优先级：数据库 > 文件
    - `getAppConfigStringValue()`、`putAppConfigFromFile()`、`putAppConfigFromDB()`

5. **HTTP 客户端工厂** (`lib/src/tools/HttpClientFactory.kt`)
    - 共享的 HTTP 客户端配置，带序列化

6. **附加信息系统**
    - `AssociatedTypeInterface` - 定义关联类型（动漫、季度、剧集等）
    - `AdditionTypeInterface` - 定义附加类型（TmdbJson、BgmJson、状态等）
    - DTO 中通过 `additions` 列表携带附加信息
    - 各状态字段（`downloadStatus`、`fileStatus`、`watchStatus`）自动从附加信息解析

### 前端（客户端）

**技术栈：**
- Compose Multiplatform 用于 UI（支持 Desktop JVM 与 WebAssembly Wasm-JS 网页端）
- Material 3 组件
- Navigation 3 组件
- Material 3 Adaptive
- Material 3 Adaptive Navigation Suite
- Lifecycle ViewModel
- Ktor Client 用于 API 调用
- Multiplatform Settings 用于跨平台本地存储（桌面端使用 Properties 文件，Web 端使用 LocalStorage）

**模块化结构 (`common-client/src/`)：**

1. **Base 模块** (`common-client/src/base/`)
   - **API** (`api/`)
     - `SyAcgnApi.kt` - 主 API 客户端，惰性初始化 HTTP 客户端
   - **Components** (`components/`)
     - 全局 Loading 状态、遮罩层与图片组件 (`AttachImage.kt` 等)
   - **Navigation** (`navigation/`)
     - `NavigationRoute.kt` - 路由定义（密封接口）
     - `NavigationAction.kt` - 基于栈的导航动作处理器
     - `NavigationWrapper.kt` - 自适应导航包装器
   - **Enums** (`enums/`)
   - **Interfaces** (`interfaces/`)
     - `Paging.kt` - 分页接口
   - **Utils** (`utils/`)
     - `LocalSettings.kt` - 本地设置管理（多平台 expect/actual）
     - `AppDirectories.kt` - 桌面端应用程序目录与存储管理
     - `FieldUtils.kt` - 字段工具

2. **Anime 模块** (`common-client/src/anime/`)
   - **API** (`api/`)
     - `AnimeApi.kt` - 动漫 API 客户端
     - `RssApi.kt` - RSS API 客户端
   - **Components** (`components/`)
   - **Pages** (`pages/`) - 动漫列表、季度详情页（含集数列表）
   - **Service** (`service/`)
   - **Enums** (`enums/`)

3. **Game 模块** (`common-client/src/game/`)
   - **API** (`api/`)
     - `GameApi.kt` - 游戏 API 客户端
   - **Pages** (`pages/`) - 游戏列表、数据同步与封面图片上传管理页面

4. **Novel 模块** (`common-client/src/novel/`)
   - **API** (`api/`)
     - `NovelApi.kt` - 小说 API 客户端
   - **Pages** (`pages/`) - 小说列表、Bangumi 导入对话框

5. **Common 模块** (`common-client/src/common/`)
   - **API** (`api/`)
     - `CommonApi.kt` - 通用 API 客户端

**核心组件详解：**

1. **桌面应用入口** (`desktop-client/src/Main.kt`)
    - 单窗口应用程序，最大化状态
    - 运行命令：`./kotlin run -m desktop-client`

2. **WebAssembly 网页应用入口** (`wasm-client/src/Main.kt`)
    - 基于 `wasm-js/app` 产品类型，挂载 Compose UI 到浏览器
    - 运行命令：`./kotlin run -m wasm-client`
    - 构建命令：`./kotlin build -m wasm-client`

3. **主应用** (`common-client/src/App.kt`)
    - 首次启动时显示服务器配置界面
    - `SyAcgnApi` 初始化和验证
    - `AcgnNavigationWrapper` 用于路由
    - `AppState` 数据类保存导航状态

5. **API 客户端** (`common-client/src/base/api/SyAcgnApi.kt`)
    - 惰性初始化的 Ktor HTTP 客户端
    - 模块化 API 访问：`rss`、`anime`、`game`、`novel`、`common`

6. **导航系统 (Navigation 3)** (`common-client/src/base/navigation/`)
    - **NavigationRoute.kt** - 密封接口定义路由，包含：
        - `NavigationRoute` 密封接口（包含 `icon` 属性）
        - `RssRoute`、`AnimeSeasonRoute` 等数据对象实现
        - `TopLevelRouteEnum` 枚举定义顶层路由
    - **NavigationAction.kt** - 基于栈的导航动作处理器
        - 使用 `LinkedHashMap` 为每个顶层路由维护独立的导航栈
        - 支持 `addTopLevel()`、`add()`、`removeLast()` 操作
        - 通过 `backStack` 暴露完整的导航历史
    - **NavigationWrapper.kt** - 自适应导航包装器
        - 使用 `NavigationSuiteScaffoldLayout` 实现自适应导航
        - 根据窗口大小自动切换底部导航栏或永久抽屉式导航
        - 使用 `NavDisplay` 和 `entryProvider` 进行路由渲染
        - 包含 `AcgnBottomNavigationBar` 和 `PermanentNavigationDrawerContent` 组件

## 配置

### 环境变量（.env）

项目支持通过 `.env` 文件配置环境变量。服务器启动时，`Main.kt` 使用 `dotenv-kotlin` 加载 `.env` 文件并将变量写入 System Properties，使 Ktor 配置文件能够引用这些变量。

`.env.example` 包含以下环境变量：

```bash
# PostgreSQL 数据库配置
SY_POSTGRESQL_HOST=localhost
SY_POSGRESQL_PORT=5432
SY_POSTGRESQL_DATABASE=sy_acgn
SY_POSTGRESQL_USER=sy_acgn
SY_POSTGRESQL_PASSWORD=sy_acgn

# 外部服务 API
SY_TMDB_API_KEY=          # TMDB API 密钥
SY_QB_API_BASE_URL=       # qBittorrent WebUI 基础 URL
SY_QB_USER_NAME=          # qBittorrent 用户名
SY_QB_PASSWORD=           # qBittorrent 密码
SY_BGM_USER_AGENT=        # Bangumi API User-Agent
```

### 后端服务配置 (`server/resources/application.yaml`)

- Ktor 服务器设置（端口 9390，主机 127.0.0.1）
- PostgreSQL 连接详情

配置解析语法：
- `$VAR:default` - 从系统属性/环境变量获取值，未找到则返回默认值
- `$?VAR` - 可选的环境变量，值可为空

### 配置优先级

1. 数据库值（最高优先级）
2. 文件值（`server/resources/application.yaml`）
3. `AppConfigInterface` 实现类中定义的默认值

## 关键模式

### 响应处理
- `MessageException` 包装 `ErrorMessage` 用于类型化错误
- 服务器捕获所有异常并返回 `Result<T>(failed=true, message=...)`
- 客户端使用 `onSuccess()` / `onSuccessData()` 扩展函数

### 依赖注入
- 使用 Ktor 内置 DI 插件（`io.ktor:ktor-server-di`）
- `configureDependencyInjection()` 中注册所有 Repository、Service 和工具类
- 路由中通过 `by application.dependencies` 委托获取依赖

### 缓存策略
- `AnimeCacheTool` / `GameCacheTool` / `NovelCacheTool` 使用 Caffeine 缓存库
- 双缓存：列表缓存 + ID 查询缓存
- 24 小时自动过期，最大 10000 条目，提高检索吞吐量并减轻数据库压力

### 本地化流程
1. 服务器在启动时加载本地化文件 (`server/resources/localization/`)
2. 客户端调用 `/api/common/info` 获取当前语言本地化
3. `LocalizationTool.putAll()` 存储字符串
4. `Key.meaning` 属性自动解析本地化文本

### 外部服务集成
- **TMDB** - 搜索电视剧/电影，获取详情和季度信息
- **Bangumi** - 搜索动漫/游戏/小说条目，获取详情并支持批量导入与数据同步
- **Steam** - 获取游戏基本信息及图片数据并进行同步
- **qBittorrent** - Torrent 下载管理和 RSS 订阅
- **文件管理** - 媒体库目录结构生成、视频/字幕文件排序和重命名

## 测试

服务器测试位于 `server/test/`，包含：
- `ApplicationTest.kt` - 应用测试
- `YdyTest.kt` - 自定义测试
- `rest-api.http` - REST API 测试文件
- `*.json` - 测试用 JSON 数据（TMDB TV/Season 详情）

## Docker

`Dockerfile` 构建服务器：
1. 基于 `azul/zulu-openjdk:25-jre` 镜像
2. 复制 `./kotlin package` 生成的可执行 JAR 包
3. 暴露端口 9390
4. 使用 `java -jar` 运行服务器

CI 脚本 (`ci.sh`) 会自动打包并构建 Docker 镜像，推送到阿里云容器镜像仓库。

## 架构最佳实践

### 模块化组织原则

本项目采用严格的模块化架构，所有代码按功能域划分为独立模块：

1. **Base 模块**：提供核心基础设施
   - 插件系统（路由、序列化、数据库、依赖注入等）
   - 通用接口和抽象类型
   - 跨模块共享的工具类

2. **功能模块**（Anime、Common 等）：
   - 每个模块包含完整的垂直分层（Entity → Repository → Service → Route）
   - 模块之间通过共享库（`lib`）中的 DTO 和接口通信
   - 每个模块可独立开发和测试

### 分层架构

**后端三层架构：**

```
Route (API 端点)
  ↓
Service (业务逻辑)
  ↓
Repository (数据访问)
  ↓
Entity (数据模型 / DAO)
```

**前端架构：**

```
Page (UI 页面)
  ↓
Service (业务逻辑协调)
  ↓
API Client (HTTP 请求)
  ↓
Component (可复用 UI 组件)
```

### 代码组织规范

1. **接口优先设计**
   - 所有 Repository 和 Service 先定义接口
   - 实现类放在独立的 `*Impl.kt` 文件中
   - 便于单元测试和依赖注入

2. **文件命名约定**
   - 接口：`XxxRepository.kt`、`XxxService.kt`
   - 实现：`XxxRepositoryImpl.kt`、`XxxServiceImpl.kt`
   - DTO：使用业务名称（如 `Anime.kt`、`Rss.kt`）
   - 路由：`XxxRoute.kt`、`XxxModuleRoute.kt`
   - 工具类：`XxxTool.kt`

3. **模块内组织**
   ```
   module-name/
   ├── entity/      # 数据表定义和 DAO
   ├── dto/         # 数据传输对象（在 lib 中）
   ├── repository/  # 数据访问层
   ├── service/     # 业务逻辑层
   ├── routes/      # API 路由
   ├── tools/       # 模块专用工具
   ├── config/      # 模块配置
   └── enums/       # 枚举和消息定义
   ```

### 依赖管理

- **Kotlin Toolchain 模板管理**：使用模板文件统一依赖版本
- **模块依赖**：
  - `server` → `lib`（服务器依赖共享库）
  - `common-client` → `lib`（客户端依赖共享库）
  - `desktop-client` → `common-client`（桌面入口依赖共享客户端）
- **导出依赖**：`common-client` 使用 `exported` 标记导出 Compose 依赖

### 类型安全

1. **枚举驱动的本地化**
   - 所有用户可见字符串通过枚举定义
   - 自动生成本地化键
   - 编译时检查完整性

2. **Result 类型**
   - 统一的错误处理机制
   - 类型安全的成功/失败判断
   - 支持空值检查

3. **密封类型**
   - Navigation 使用密封接口定义路由
   - 编译时保证路由完整性

4. **Resource 路由**
   - 使用 Ktor Resources 插件定义类型安全的路由
   - 嵌套的 `@Resource` 注解类构建完整 URL 路径
   - 编译时 URL 参数类型检查

## 最近重大改进

### 2026-08 功能更新与重构

1. **WebAssembly (Wasm-JS) 客户端支持与多平台改造**
   - 依据 Kotlin Toolchain 官方规范，添加 `wasm-client` 模块（产品类型 `wasm-js/app`），支持直接编译为 WebAssembly 并在现代浏览器中运行
   - 升级共享库 `lib` 与 UI 组件 `common-client` 为多平台库（同时支持 `jvm` 与 `wasmJs`）
   - 重构跨平台存储（`LocalSettings` 使用 `expect/actual` 分别对接桌面 Properties 与浏览器 LocalStorage）
   - 适配跨平台图片解码（采用 Compose Resources `decodeToImageBitmap` 代替 ImageIO）与纯 Kotlin 缓存机制
   - 采用多平台 `AtomicInt` 与 `PlatformLock` 抽象，确保线程与并发安全

2. **Game（游戏）功能模块全栈支持**
   - 新增 Game 模块 API、数据库表定义、服务层及前端 UI 界面
   - 支持新增与更新游戏，可选择从 Bangumi 或 Steam 来源同步游戏元数据
   - 支持游戏封面图的上传、预览与本地存储管理
   - 引入基于 Caffeine 的 `GameCacheTool` 优化列表查询性能

2. **Novel（小说）功能模块与性能优化**
   - 新增 Bangumi 导入对话框及小说搜索功能
   - 使用 Kotlin 协程并行化优化小说列表及卷列表加载逻辑，提升并发吞吐
   - 引入基于 Caffeine 的 `NovelCacheTool` 缓存机制，移除硬编码的分页限制

3. **前端 Compose 体验与界面增强**
   - 新增动画季度详情页，支持展示季度元数据与集数列表，优化季度导航逻辑
   - 添加全局 Loading 状态和遮罩层，提升界面数据请求过程中的交互反馈
   - 优化动画季度页面标签显示（使用 Compose `AssistChip` 规范化呈现）
   - 统一整理客户端应用目录存储工具 `AppDirectories.kt`

### 2026-02 新功能

1. **环境变量支持**
   - 引入 `dotenv-kotlin` 加载 `.env` 文件
   - 环境变量写入 System Properties 供 Ktor 配置引用
   - 生产环境自动忽略缺失的 `.env` 文件

2. **Bangumi 集成**
   - 新增 `BangumiTool` 支持 Bangumi API
   - 搜索动漫、获取条目详情
   - 自动转换为 `AnimeSeason` 数据结构

3. **Ktor DI 依赖注入**
   - 使用 Ktor 内置 DI 插件替代手动管理
   - 集中注册所有 Repository、Service 和工具类

### 2026-01 代码重构

1. **分离接口与实现**
   - 将所有 `*Impl` 类移至独立文件
   - 提高代码可维护性和可测试性
   - 影响范围：所有 Repository 和 Service 类

2. **模块化重组**
   - 服务器代码重组为 `base/`、`anime/`、`common/` 模块
   - 共享库同步重组，按模块划分
   - 客户端代码采用相同的模块化结构

3. **导航系统升级**
   - 升级到 Navigation 3
   - 实现自适应导航（底部栏 ↔ 侧边栏）
   - 独立的导航栈管理

## 重要说明

- **使用 Kotlin Toolchain 而非 Gradle** - 所有命令通过 `./kotlin` 进行
- **环境变量** 通过 `.env` 文件配置，使用 `dotenv-kotlin` 加载
- **依赖注入** 使用 Ktor 内置 DI 插件（`io.ktor:ktor-server-di`）
- **配置解析** 在 `application.yaml` 中使用 `$VAR:default` 和 `$?VAR` 语法
- **多语言** 基于枚举，自动生成键
- **数据库自动迁移** 在启动时使用 Exposed 迁移完成
- **双层配置系统**：文件 + 数据库，有明确的优先级
- **共享代码** 在 `lib/` 和 `common-client/` 中，供服务器和桌面客户端共用
- **模块化架构** 每个功能模块独立管理，接口与实现分离
- **缓存** 使用 Caffeine 库实现服务端数据缓存
- **日志** 使用 `private val logger = KotlinLogging.logger { }`
