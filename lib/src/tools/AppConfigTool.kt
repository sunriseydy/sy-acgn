package dev.sunriseydy.acgn.tools

import com.russhwolf.settings.Settings
import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig
import dev.sunriseydy.acgn.common.dto.AppConfig

/**
 * @author SunriseYDY
 * @date 2024-07-12 16:47
 */
object AppConfigTool {
    /**
     * 持久化的配置
     */
    private val serverSettings: Settings = Settings()

    fun getLocalServerConfig(): String? = serverSettings.getStringOrNull(CommonModuleAppConfig.AppServer.configKey)
    fun setLocalServerConfig(value: String) = serverSettings.putString(CommonModuleAppConfig.AppServer.configKey, value)

    private val appConfigs =
        mutableMapOf<String/* config key */, Pair<AppConfig?/* db value */, String?/* file value */>>()

    fun getAppConfigStringValue(key: String): String? {
        return appConfigs[key]?.first?.configValue ?: appConfigs[key]?.second
    }

    fun getAppConfigs() = appConfigs

    fun putAppConfigFromFile(key: String, value: String) {
        appConfigs[key] = Pair(appConfigs[key]?.first, value)
    }

    fun putAppConfigFromDB(key: String, value: AppConfig) {
        appConfigs[key] = Pair(value, appConfigs[key]?.second)
    }

    fun fromAppConfigList(appConfigList: List<AppConfig>) {
        appConfigList.forEach {
            this.putAppConfigFromDB(it.configKey, it)
        }
    }
}