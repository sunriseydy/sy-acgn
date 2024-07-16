package dev.sunriseydy.acgn.plugins

import dev.sunriseydy.acgn.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.Application
import io.ktor.server.config.yaml.YamlConfig

/**
 * @author SunriseYDY
 * @date 2024-07-14 20:32
 */
suspend fun Application.loadAppConfig() {
    // 先从配置文件中加载配置
    AppConfigTool.loadAppConfigFromFile()
    // 再从数据库中加载配置
    AppConfigTool.loadAppConfigFromDB()
}

suspend fun AppConfigTool.loadAppConfigFromDB() = AppConfigService().loadAppConfig()
fun AppConfigTool.loadAppConfigFromFile() {
    val yamlConfig = YamlConfig("config/config.yaml") ?: return
    yamlConfig.keys().forEach {
        val key = it
        val value = yamlConfig.propertyOrNull(it)?.getString()
        value?.let {
            putAppConfig(key, value)
        }
    }
}