package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.common.dto.AppConfig

/**
 * @author SunriseYDY
 * @date 2024-07-12 16:47
 */
object AppConfigTool {
    val appConfig = mutableMapOf<String, String>()

    fun fromAppConfigList(appConfigList: List<AppConfig>) {
        appConfigList.forEach {
            appConfig[it.configKey] = it.configValue
        }
    }
}