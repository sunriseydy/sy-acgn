package dev.sunriseydy.acgn.common.dto

import kotlinx.datetime.Instant

/**
 * @author SunriseYDY
 * @date 2024-07-12 13:14
 */
data class AppConfig(
    val id: ULong,
    val configKey: String,
    val configValue: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
