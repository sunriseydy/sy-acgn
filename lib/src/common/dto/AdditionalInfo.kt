@file:UseSerializers(OffsetDateTimeSerializer::class)
package dev.sunriseydy.acgn.common.dto

import dev.sunriseydy.acgn.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.OffsetDateTime

/**
 * @author SunriseYDY
 * @date 2024-07-13 16:53
 */
@Serializable
data class AdditionalInfo(
    val id: String,
    /**
     * 关联id
     */
    val associatedId: ULong,
    /**
     * 关联类型
     */
    val associatedType: String,
    /**
     * 附加类型
     */
    val additionalType: String,
    /**
     * 附加值
     */
    val additionalValue: String,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
