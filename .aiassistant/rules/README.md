---
apply: 始终
---

# AI Assistant

## 项目概述

SY-ACGN 是一个基于 Kotlin 的动漫管理系统，包含 Ktor 后端服务器、Compose Multiplatform 桌面客户端和共享库模块。项目使用 **Amper** 作为构建工具。

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
- `./server` - Ktor 后端服务器
- `./lib` - 共享 Kotlin 库（通用 DTO、工具、接口）
- `./common-client` - 共享 Compose UI 组件和 API 客户端
- `./desktop-client` - 桌面应用程序入口点

每个模块都有一个 `.module.yaml` 文件定义其依赖关系。

## 架构

### 后端（服务器）

**技术栈：**
- Ktor（CIO 引擎）作为 HTTP 服务器
- Exposed（1.0.0-rc-4）用于 PostgreSQL 数据库访问
- Kotlinx Serialization 用于 JSON 序列化
- Kotlin Logging

**核心插件 (`server/src/plugins/`)：**
- `Application.kt` - 主入口点，注册所有插件
- `Databases.kt` - PostgreSQL 连接和模式迁移
- `AppConfig.kt` - 从文件和数据库加载配置
- `Localization.kt` - 加载多语言字符串
- `Routing.kt` - API 路由配置和错误处理
- `Serialization.kt` - JSON 序列化设置
- `HTTP.kt` - HTTP 客户端配置
- `Monitoring.kt` - 日志记录设置

**基于模块的路由：**
- `/api/common/*` - 通用模块路由（应用信息、配置、本地化）
- `/api/anime/*` - 动漫模块路由（动漫、RSS）

**数据库模式迁移：**
使用 Exposed 的 `MigrationUtils.statementsRequiredForDatabaseMigration()` 自动迁移表：
- `rssTables()` - RSS 订阅相关表
- `animeTables()` - 动漫元数据表
- `commonModuleTables()` - 通用模块表（app_config, additional_info）

### 共享库（Lib）

**核心组件：**

1. **Result 模式** (`lib/src/Result.kt`)
    - 通用 `Result<T>` 类，包含 `failed`、`message`、`data` 字段
    - `checkSuccess()` 和 `checkSuccessAndNotNull()` 辅助方法

2. **多语言系统** (`lib/src/interfaces/KeyInterface.kt`)
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
- Material 3 组件
- 导航组件
- Ktor Client 用于 API 调用

**核心组件：**

1. **应用入口** (`desktop-client/src/Main.kt`)
    - 单窗口应用程序，最大化状态
    - 1200x1000 dp 窗口大小

2. **主应用** (`common-client/src/App.kt`)
    - 首次启动时显示服务器配置界面
    - `SyAcgnApi` 初始化和验证
    - `AcgnNavigationWrapper` 用于路由
    - `AppState` 数据类保存导航状态

3. **API 客户端** (`common-client/src/SyAcgnApi.kt`)
    - 惰性初始化的 Ktor HTTP 客户端
    - 模块化 API 访问：`rss`、`anime`、`common`
    - `apiEndPoint()`、`animeModuleApiEndPoint()`、`commonModuleApiEndPoint()` 辅助函数

4. **导航** (`common-client/src/navigation/`)
    - `AcgnNavigationRoute.kt` - 路由定义
    - `AcgnNavigationAction.kt` - 导航动作
    - `AcgnNavigationHost.kt` - Compose 导航主机
    - `AcgnNavigationWrapper.kt` - 包装器（Snackbar、主题）

5. **UI 组件** (`common-client/src/components/`)
    - `AcgnLazyColumn` - 带加载状态的懒加载列表
    - `FormCard`、`FormDialog` - 表单组件
    - `ServerConfig` - 服务器连接配置
    - `AcgnSnackbarHost` - 错误/成功通知

## 配置

### 静态配置 (`server/resources/application.yaml`)
- Ktor 服务器设置（端口 9390，主机 127.0.0.1）
- PostgreSQL 连接详情
- 配置解析：`$VAR:default` 语法用于环境变量

### 动态配置 (`server/resources/data/config/`)
- `config.yaml` - 默认配置
- `config.local.yaml` - 本地覆盖（git 忽略）
- 启动时加载到 `AppConfigTool`

### 本地化文件 (`server/resources/data/localization/`)
- `zh-CN.yaml` - 简体中文
- `en-US.yaml` - 英文
- 根据 `AppLanguage` 配置加载

## 关键模式

### 错误处理
- `MessageException` 包装 `ErrorMessage` 用于类型化错误
- 服务器捕获所有异常并返回 `Result<T>(failed=true, message=...)`
- 客户端使用 `onSuccess()` / `onSuccessData()` 扩展函数

### 配置优先级
1. 数据库值（最高优先级）
2. 文件值（`config.local.yaml` > `config.yaml`）
3. `AppConfigInterface` 实现中定义的默认值

### 本地化流程
1. 服务器在启动时加载本地化文件
2. 客户端调用 `/common/info` 获取当前本地化
3. `LocalizationTool.putAll()` 存储字符串
4. `Key.meaning` 属性自动解析本地化文本

## 测试

服务器测试位于 `server/test/`。当前为最小化设置。

## Docker

`Dockerfile` 构建服务器：
1. 使用 Amper 编译
2. 基于 Alpine 创建包含编译产物的镜像
3. 容器启动时运行服务器

## 重要说明

- **使用 Amper 而非 Gradle** - 所有命令通过 `./amper` 进行
- **配置解析** 在 `application.yaml` 中使用自定义的 `$VAR:default` 语法
- **多语言** 基于枚举，自动生成键
- **数据库自动迁移** 在启动时使用 Exposed 迁移完成
- **双层配置系统**：文件 + 数据库，有明确的优先级
- **共享代码** 在 `lib/` 和 `common-client/` 中，供服务器和客户端共用