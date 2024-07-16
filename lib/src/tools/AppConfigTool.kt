package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.common.dto.AppConfig

/**
 * @author SunriseYDY
 * @date 2024-07-12 16:47
 */
object AppConfigTool {
    private val appConfig = mutableMapOf<String, String>()

    fun getAppConfig(key: String): String? {
        return appConfig[key]
    }

    fun getAppConfigs(): Map<String, String> {
        return appConfig
    }

    fun putAppConfig(key: String, value: String) {
        appConfig[key] = value
    }

    fun fromAppConfigList(appConfigList: List<AppConfig>) {
        appConfigList.forEach {
            appConfig[it.configKey] = it.configValue
        }
    }
}