package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.*
import io.ktor.server.config.*

/**
 * AppConfig 加载扩展函数
 *
 * 提供从数据库和配置文件加载应用配置的能力。
 *
 * @author SunriseYDY
 * @date 2024-07-14 20:32
 */

/**
 * 从数据库加载应用配置
 *
 * 读取数据库中的所有配置项并写入 AppConfigTool，覆盖文件中的默认值。
 */
suspend fun AppConfigTool.loadAppConfigFromDB(appConfigService: AppConfigService) = fromAppConfigList(
    appConfigService.getAllAppConfigFromDB())

/**
 * 从 Ktor YAML 配置文件加载应用配置
 *
 * 扫描所有以 "config" 前缀开头的配置项，写入 AppConfigTool 作为文件级默认值。
 */
fun AppConfigTool.loadAppConfigFromFile(environment: ApplicationEnvironment) {
    val yamlConfig = environment.config
    yamlConfig.keys().forEach { key ->
        if (key.startsWith("config")) {
            yamlConfig.propertyOrNull(key)?.let { property: ApplicationConfigValue ->
                putAppConfigFromFile(key, property.getString())
            }
        }
    }
}