package dev.sunriseydy.acgn.common.dto

import kotlinx.serialization.Serializable

/**
 * @author SunriseYDY
 * @date 2024-08-08 23:33
 */
@Serializable
data class AppInfo(
    val version: String = "0.0.1",
    val configs: MutableMap<String, Pair<AppConfig?, String?>>,
    val localizations: MutableMap<String, String>,
)
