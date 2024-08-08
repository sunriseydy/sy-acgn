package dev.sunriseydy.acgn.ui

import com.russhwolf.settings.Settings
import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig

/**
 * @author SunriseYDY
 * @date 2024-08-08 21:42
 */
private val localSettings: Settings = Settings()

fun getLocalServerConfig(): String? = localSettings.getStringOrNull(CommonModuleAppConfig.AppServer.configKey)
fun setLocalServerConfig(value: String): String =
    localSettings.putString(CommonModuleAppConfig.AppServer.configKey, value)
        .run { getLocalServerConfig() ?: throw Error("设置 server 失败") }

fun clearLocalServerConfig() = localSettings.clear()