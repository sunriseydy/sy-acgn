package dev.sunriseydy.acgn.common.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * 附件文件信息 DTO
 *
 * @property id 附件 ID (UUID String)
 * @property fileName 文件名
 * @property fileKey S3 中的对象键名
 * @property contentType 媒体类型
 * @property fileSize 文件大小
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 *
 * @author SunriseYDY
 * @date 2026-07-06
 */
@Serializable
data class AttachFileInfo(
    val id: String,
    val fileName: String,
    val fileKey: String,
    val contentType: String,
    val fileSize: Long,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
