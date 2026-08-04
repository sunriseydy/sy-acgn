package dev.sunriseydy.acgn.client.base.utils

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import java.io.File
import java.util.Properties

/**
 * @author SunriseYDY
 * @date 2024-08-08 21:42
 */

const val APP_SERVER_CONFIG_KEY = "app.server"

private val settingsFile: File = File(AppDirectories.appDataDir, "settings.properties")

private fun loadProperties(): Properties {
    val properties = Properties()
    if (settingsFile.exists()) {
        try {
            settingsFile.inputStream().use { properties.load(it) }
        } catch (e: Exception) {
            // Ignore load error
        }
    } else {
        try {
            val legacySettings = Settings()
            val legacyConfig = legacySettings.getStringOrNull(APP_SERVER_CONFIG_KEY)
            if (!legacyConfig.isNullOrBlank()) {
                properties.setProperty(APP_SERVER_CONFIG_KEY, legacyConfig)
                saveProperties(properties)
            }
        } catch (e: Exception) {
            // Ignore migration error
        }
    }
    return properties
}

private fun saveProperties(properties: Properties) {
    try {
        settingsFile.parentFile?.mkdirs()
        settingsFile.outputStream().use { properties.store(it, "SY-ACGN Settings") }
    } catch (e: Exception) {
        // Ignore save error
    }
}

private val localSettings: Settings = PropertiesSettings(
    loadProperties(),
    ::saveProperties
)

fun getLocalServerConfigOrNull(): String? = localSettings.getStringOrNull(APP_SERVER_CONFIG_KEY)
fun getLocalServerConfig(): String = checkNotNull(getLocalServerConfigOrNull())
fun setLocalServerConfig(value: String): String =
    localSettings.putString(APP_SERVER_CONFIG_KEY, value)
        .run { getLocalServerConfig() }

fun clearLocalServerConfig() = localSettings.clear()