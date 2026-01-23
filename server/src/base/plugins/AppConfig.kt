package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.*
import io.ktor.server.config.*

/**
 * @author SunriseYDY
 * @date 2024-07-14 20:32
 */
suspend fun AppConfigTool.loadAppConfigFromDB(appConfigService: AppConfigService) = fromAppConfigList(
    appConfigService.getAllAppConfigFromDB())

fun AppConfigTool.loadAppConfigFromFile(environment: ApplicationEnvironment) {
    val yamlConfig = environment.config
    yamlConfig.keys().forEach {
        val key = it
        if (key.startsWith("config"))
            yamlConfig.propertyOrNull(key)?.let { it: ApplicationConfigValue -> putAppConfigFromFile(key, it.getString()) }
    }
}