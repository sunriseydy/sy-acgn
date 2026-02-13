# SY ACGN

## 目录

- [项目概述](#项目概述)
  - [快速开始](#快速开始)
- [构建系统：Amper](#构建系统amper)
  - [常用命令](#常用命令)
  - [模块结构](#模块结构)
- [架构](#架构)
  - [后端（服务器）](#后端服务器)
  - [共享库（Lib）](#共享库lib)
  - [前端（桌面客户端）](#前端桌面客户端)
- [配置](#配置)
- [关键模式](#关键模式)
- [测试](#测试)
- [Docker](#docker)
- [架构最佳实践](#架构最佳实践)
- [最近重大改进](#最近重大改进)
- [重要说明](#重要说明)

## 项目概述

SY-ACGN 是一个基于 Kotlin 的动漫管理系统，包含 Ktor 后端服务器、Compose Multiplatform 桌面客户端和共享库模块。项目使用 **Amper** 作为构建工具。

### 快速开始

```bash
# 运行服务器
./run-server.sh

# 运行桌面客户端
./run-desktop.sh

# 编译服务器
./amper build -m server

# CI 构建（编译 + Docker 镜像）
./ci.sh
```

## 构建系统：Amper

本项目使用 JetBrains Amper（版本 0.9.2）而非 Gradle。所有构建操作都通过 `./amper` 脚本进行。

### 常用命令

```bash
# 运行服务器
./run-server.sh
# 或直接：
./amper run -m server

# 运行桌面客户端
./run-desktop.sh
# 或直接：
./amper run -m desktop-client

# 编译服务器
./amper build -m server

# CI 构建（编译 + 构建 Docker 镜像）
./ci.sh
```

### 模块结构

项目在 `project.yaml` 中定义了多个模块：

```
sy-acgn/
├── server/              # Ktor 后端服务器
│   ├── src/
│   │   ├── base/       # 核心插件和配置
│   │   ├── anime/      # 动漫功能模块
│   │   ├── common/     # 通用功能模块
│   │   └── Main.kt
│   └── module.yaml
│
├── lib/                 # 共享 Kotlin 库
│   ├── src/
│   │   ├── base/       # 核心接口、Result、枚举
│   │   ├── anime/      # 动漫模块 DTO/枚举
│   │   ├── common/     # 通用模块 DTO/枚举
│   │   └── tools/      # 跨模块工具类
│   └── module.yaml
│
├── common-client/       # 共享 Compose UI 组件
│   ├── src/
│   │   ├── base/       # 导航、API、通用组件
│   │   ├── anime/      # 动漫页面和组件
│   │   └── common/     # 通用 API 客户端
│   └── module.yaml
│
└── desktop-client/      # 桌面应用入口点
    ├── src/Main.kt
    └── module.yaml
```

**依赖关系：**

```
desktop-client
    ↓
common-client  →  lib
    ↓              ↓
  server    ←────┘
```

每个模块都有一个 `module.yaml` 文件定义其依赖关系。

## 架构

### 后端（服务器）

**技术栈：**
- Ktor（CIO 引擎）作为 HTTP 服务器
- Exposed（1.0.0-rc-4）用于 PostgreSQL 数据库访问
- Kotlinx Serialization 用于 JSON 序列化
- Kotlin Logging

**核心插件 (`server/src/base/plugins/`)：**
- `Application.kt` - 主入口点，注册所有插件
- `Databases.kt` - PostgreSQL 连接和模式迁移
- `AppConfig.kt` - 从文件和数据库加载配置
- `Localization.kt` - 加载多语言字符串
- `Routing.kt` - API 路由配置和错误处理
- `Serialization.kt` - JSON 序列化设置
- `HTTP.kt` - HTTP 客户端配置
- `Monitoring.kt` - 日志记录设置

**模块化架构 (`server/src/`)：**

1. **Base 模块** (`server/src/base/`)
   - 包含核心插件和配置
   - `PostgresqlConfig.kt` - 数据库配置
   - `DatabaseKey.kt` - 数据库常量

2. **Anime 模块** (`server/src/anime/`)
   - `entity/` - 数据表定义 (`AnimeTables.kt`, `RssTables.kt`)
   - `repository/` - 数据访问层
     - `AnimeRepository.kt` + `AnimeRepositoryImpl.kt`
     - `RssRepository.kt` + `RssRepositoryImpl.kt`
   - `service/` - 业务逻辑层
     - `AnimeService.kt` + `AnimeServiceImpl.kt`
     - `RssService.kt` + `RssServiceImpl.kt`
   - `routes/` - API 路由 (`AnimeModuleRoute.kt`, `AnimeRoute.kt`, `RssRoute.kt`)
   - `tools/` - 工具类 (TMDB、RSS、qBittorrent、Torrent 等)

3. **Common 模块** (`server/src/common/`)
   - `entity/` - 通用数据表 (`CommonModuleTables.kt`)
   - `repository/` - 配置和附加信息仓储
     - `AppConfigRepository.kt` + `AppConfigRepositoryImpl.kt`
     - `AdditionalInfoRepository.kt` + `AdditionalInfoRepositoryImpl.kt`
   - `service/` - 应用配置服务
     - `AppConfigService.kt` + `AppConfigServiceImpl.kt`
   - `routes/` - 通用 API 路由 (`CommonModuleRoute.kt`)

**API 路由结构：**
- `/api/common/*` - 通用模块路由（应用信息、配置、本地化）
- `/api/anime/*` - 动漫模块路由（动漫季度、RSS 订阅）

**数据库模式迁移：**
使用 Exposed 的 `MigrationUtils.statementsRequiredForDatabaseMigration()` 自动迁移表：
- `rssTables()` - RSS 订阅相关表
- `animeTables()` - 动漫元数据表
- `commonModuleTables()` - 通用模块表（app_config, additional_info）

**分层架构模式：**
- **接口 + 实现分离**：所有 Repository 和 Service 采用接口定义 + 实现类的模式
- **实现类独立文件**：每个 `*Impl.kt` 文件包含对应接口的具体实现
- **依赖注入友好**：清晰的接口边界便于测试和依赖注入

### 共享库（Lib）

**模块化结构 (`lib/src/`)：**

1. **Base 模块** (`lib/src/base/`)
   - `Result.kt` - 通用结果类型
   - `ApiResources.kt` - API 资源定义
   - `enums/` - 基础枚举类型
     - `Language.kt` - 语言枚举
     - `MessageLevel.kt` - 消息级别
     - `ModuleName.kt` - 模块名称
     - `Status.kt` - 状态枚举
   - `exception/` - 异常类型
     - `MessageException.kt` - 消息异常
   - `interfaces/` - 核心接口
     - `KeyInterface.kt` - 多语言键接口
     - `AppConfigInterface.kt` - 应用配置接口
     - `AssociatedTypeInterface.kt` - 关联类型接口
     - `AdditionTypeInterface.kt` - 附加类型接口

2. **Anime 模块** (`lib/src/anime/`)
   - `dto/` - 数据传输对象
     - `Anime.kt` - 动漫相关 DTO
     - `Rss.kt` - RSS 相关 DTO
   - `config/` - 模块配置
     - `AnimeModuleAppConfig.kt` - 动漫模块应用配置
   - `enums/` - 模块枚举
     - `AnimeEnum.kt` - 动漫枚举
     - `AnimeModuleMessage.kt` - 动漫模块消息
   - `AnimeModuleResources.kt` - 模块资源定义

3. **Common 模块** (`lib/src/common/`)
   - `dto/` - 通用数据传输对象
     - `AppInfo.kt` - 应用信息
     - `AppConfig.kt` - 应用配置
     - `AdditionalInfo.kt` - 附加信息
   - `config/` - 通用配置
     - `CommonModuleAppConfig.kt` - 通用模块应用配置
   - `enums/` - 通用枚举
     - `CommonModuleMessage.kt` - 通用模块消息
   - `CommonModuleResources.kt` - 通用模块资源定义

4. **工具类** (`lib/src/tools/`)
   - `LocalizationTool.kt` - 本地化工具
   - `AppConfigTool.kt` - 应用配置工具
   - `HttpClientFactory.kt` - HTTP 客户端工厂

5. **序列化工具** (`lib/src/`)
   - `OffsetDateTimeSerializer.kt` - 日期时间序列化器

**核心设计模式：**

1. **Result 模式** (`lib/src/base/Result.kt`)
    - 通用 `Result<T>` 类，包含 `failed`、`message`、`data` 字段
    - `checkSuccess()` 和 `checkSuccessAndNotNull()` 辅助方法

2. **多语言系统** (`lib/src/base/interfaces/KeyInterface.kt`)
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

### 前端（桌面客户端）

**技术栈：**
- Compose Multiplatform 用于 UI
- Material 3 组件 (1.10.0-alpha05)
- Navigation 3 组件 (1.0.0-alpha06)
- Material 3 Adaptive Navigation Suite
- Ktor Client 用于 API 调用
- Multiplatform Settings 用于本地存储

**模块化结构 (`common-client/src/`)：**

1. **Base 模块** (`common-client/src/base/`)
   - **API** (`api/`)
     - `SyAcgnApi.kt` - 主 API 客户端，惰性初始化 HTTP 客户端
   - **Components** (`components/`)
     - `AcgnLazyColumn.kt` - 带加载状态的懒加载列表
     - `FormCard.kt`, `FormDialog.kt` - 表单组件
     - `ServerConfig.kt` - 服务器连接配置
     - `AcgnSnackbarHost.kt` - 错误/成功通知
     - `AcgnAlertDialog.kt` - 确认对话框
     - `EmptyComingSoon.kt` - 空状态占位符
     - `PageTitle.kt` - 页面标题组件
   - **Navigation** (`navigation/`)
     - `NavigationRoute.kt` - 路由定义（密封接口）
     - `NavigationAction.kt` - 基于栈的导航动作处理器
     - `NavigationWrapper.kt` - 自适应导航包装器
   - **Interfaces** (`interfaces/`)
     - `Paging.kt` - 分页接口
   - **Utils** (`utils/`)
     - `LocalSettings.kt` - 本地设置管理
     - `WindowUtils.kt` - 窗口工具
     - `FieldUtils.kt` - 字段工具

2. **Anime 模块** (`common-client/src/anime/`)
   - **API** (`api/`)
     - `AnimeApi.kt` - 动漫 API 客户端
     - `RssApi.kt` - RSS API 客户端
   - **Components** (`components/`)
     - `CreateAnimeSeason.kt` - 创建动漫季度组件
     - `CreateAnimeSeasonFromFile.kt` - 从文件创建动漫季度
   - **Pages** (`pages/`)
     - `AnimeSeason.kt` - 动漫季度页面
     - `Rss.kt` - RSS 订阅页面
   - **Service** (`service/`)
     - `AnimeSeasonService.kt` - 动漫季度服务
     - `RssService.kt` - RSS 服务
   - **Enums** (`enums/`)
     - `AnimeString.kt` - 动漫模块字符串
     - `RssString.kt` - RSS 字符串

3. **Common 模块** (`common-client/src/common/`)
   - **API** (`api/`)
     - `CommonApi.kt` - 通用 API 客户端
   - **Enums** (`enums/`)
     - `CommonString.kt` - 通用字符串

**核心组件详解：**

1. **应用入口** (`desktop-client/src/Main.kt`)
    - 单窗口应用程序，最大化状态
    - 1200x1000 dp 窗口大小

2. **主应用** (`common-client/src/App.kt`)
    - 首次启动时显示服务器配置界面
    - `SyAcgnApi` 初始化和验证
    - `AcgnNavigationWrapper` 用于路由
    - `AppState` 数据类保存导航状态

3. **API 客户端** (`common-client/src/base/api/SyAcgnApi.kt`)
    - 惰性初始化的 Ktor HTTP 客户端
    - 模块化 API 访问：`rss`、`anime`、`common`

4. **导航系统 (Navigation 3)** (`common-client/src/base/navigation/`)
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

## 后端服务配置 (`server/resources/application.yaml`)

- Ktor 服务器设置（端口 9390，主机 127.0.0.1）
- PostgreSQL 连接详情

配置解析：
- `$VAR:default` 语法表示环境变量+默认值
- `$?VAR` 语法表示可选的环境变量

## 关键模式

### 错误处理
- `MessageException` 包装 `ErrorMessage` 用于类型化错误
- 服务器捕获所有异常并返回 `Result<T>(failed=true, message=...)`
- 客户端使用 `onSuccess()` / `onSuccessData()` 扩展函数

### 配置优先级
1. 数据库值（最高优先级）
2. 文件值（`server/resources/application.yaml`）
3. `AppConfigInterface` 实现类中定义的默认值

### 本地化流程
1. 服务器在启动时加载本地化文件 (`server/resources/localization/`)
2. 客户端调用 `/common/info` 获取当前语言本地化
3. `LocalizationTool.putAll()` 存储字符串
4. `Key.meaning` 属性自动解析本地化文本

## 测试

服务器测试位于 `server/test/`。当前为最小化设置。

## Docker

`Dockerfile` 构建服务器：
1. 使用 Amper 编译
2. 基于 Alpine 创建包含编译产物的镜像
3. 容器启动时运行服务器

## 架构最佳实践

### 模块化组织原则

本项目采用严格的模块化架构，所有代码按功能域划分为独立模块：

1. **Base 模块**：提供核心基础设施
   - 插件系统（路由、序列化、数据库等）
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
Entity (数据模型)
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

3. **模块内组织**
   ```
   module-name/
   ├── entity/      # 数据表定义
   ├── dto/         # 数据传输对象（在 lib 中）
   ├── repository/  # 数据访问层
   ├── service/     # 业务逻辑层
   ├── routes/      # API 路由
   ├── tools/       # 模块专用工具
   └── enums/       # 枚举和消息定义
   ```

### 依赖管理

- **Amper 版本管理**：使用模板文件统一依赖版本
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

## 最近重大改进

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

- **使用 Amper 而非 Gradle** - 所有命令通过 `./amper` 进行
- **配置解析** 在 `application.yaml` 中使用自定义的 `$VAR:default` 语法
- **多语言** 基于枚举，自动生成键
- **数据库自动迁移** 在启动时使用 Exposed 迁移完成
- **双层配置系统**：文件 + 数据库，有明确的优先级
- **共享代码** 在 `lib/` 和 `common-client/` 中，供服务器和客户端共用
- **模块化架构** 每个功能模块独立管理，接口与实现分离
- **日志** 使用 `private val logger = KotlinLogging.logger { }`
