package dev.sunriseydy.acgn.common.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable
/**
 * @author SunriseYDY
 * @date 2024-07-12 13:14
 */
@Serializable
data class AppConfig(
    val id: ULong,
    val configKey: String,
    val configValue: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
