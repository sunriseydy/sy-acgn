package dev.sunriseydy.acgn.client

import com.russhwolf.settings.Settings

/**
 * @author SunriseYDY
 * @date 2024-08-08 21:42
 */

const val APP_SERVER_CONFIG_KEY = "app.server"

private val localSettings: Settings = Settings()

fun getLocalServerConfigOrNull(): String? = localSettings.getStringOrNull(APP_SERVER_CONFIG_KEY)
fun getLocalServerConfig(): String = checkNotNull(getLocalServerConfigOrNull())
fun setLocalServerConfig(value: String): String =
    localSettings.putString(APP_SERVER_CONFIG_KEY, value)
        .run { getLocalServerConfig() }

fun clearLocalServerConfig() = localSettings.clear()