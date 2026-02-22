@file:UseSerializers(OffsetDateTimeSerializer::class)
package dev.sunriseydy.acgn.common.dto

import dev.sunriseydy.acgn.base.serializer.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.OffsetDateTime

/**
 * @author SunriseYDY
 * @date 2024-07-12 13:14
 */
@Serializable
data class AppConfig(
    val id: ULong,
    val configKey: String,
    val configValue: String,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
