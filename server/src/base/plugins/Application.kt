package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

/**
 * Ktor 应用主模块
 *
 * 在此配置所有的服务插件，包括序列化、监控、HTTP、依赖注入、数据库、本地化和路由。
 * 并按顺序初始化应用配置。
 */
suspend fun Application.module() {
    // 1. 配置序列化（JSON）
    configureSerialization()
    // 2. 配置日志监控
    configureMonitoring()
    // 3. 配置 HTTP 客户端和服务器特性
    configureHTTP()
    // 4. 配置依赖注入 (DI)
    configureDependencyInjection()
    
    // 5. 从配置文件加载应用配置
    AppConfigTool.loadAppConfigFromFile(environment)
    
    // 6. 配置数据库连接和迁移
    configureDatabases()
    
    // 7. 从数据库加载应用配置（覆盖文件配置）
    AppConfigTool.loadAppConfigFromDB(dependencies.resolve<AppConfigService>())
    
    // 8. 配置本地化/多语言
    configureLocalization()
    
    // 9. 配置路由（API 端点）
    configureRouting()
}