package dev.sunriseydy.acgn.common.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable
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
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
