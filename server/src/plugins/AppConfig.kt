package dev.sunriseydy.acgn.server.plugins

import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.Application
import io.ktor.server.config.yaml.YamlConfig
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-07-14 20:32
 */
fun Application.loadAppConfig() {
    runBlocking {
        AppConfigTool.refreshAppConfig()
    }
}

suspend fun AppConfigTool.loadAppConfigFromDB() = fromAppConfigList(AppConfigService().getAllAppConfigFromDB())

fun AppConfigTool.loadAppConfigFromFile() {
    val yamlConfig = YamlConfig("data/config/config.local.yaml") ?: YamlConfig("data/config/config.yaml") ?: return
    yamlConfig.keys().forEach {
        val key = it
        yamlConfig.propertyOrNull(it)?.let { putAppConfigFromFile(key, it.getString()) }
    }
}

suspend fun AppConfigTool.refreshAppConfig() {
    // 先从配置文件中加载配置
    loadAppConfigFromFile()
    // 再从数据库中加载配置
    loadAppConfigFromDB()
}