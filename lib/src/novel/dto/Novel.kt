package dev.sunriseydy.acgn.novel.dto

import dev.sunriseydy.acgn.base.interfaces.AdditionInterface
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.novel.enums.NovelAdditionType
import dev.sunriseydy.acgn.novel.enums.NovelStatusEnum
import dev.sunriseydy.acgn.novel.enums.ReadingStatusEnum
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * 轻小说系列 DTO
 *
 * @property id 小说 ID
 * @property name 小说名称（中文/通用名）
 * @property originalName 日语原名/原标题
 * @property author 作者
 * @property illustrator 插画家/绘师
 * @property description 简介
 * @property publisher 文库/出版社
 * @property status 连载状态 [NovelStatusEnum]
 * @property totalVolumes 总卷数
 * @property bgmId Bangumi 关联 ID
 * @property volumes 关联的卷列表
 * @property additions 附加信息列表
 */
@Serializable
data class Novel(
    val id: ULong,
    val name: String,
    val originalName: String? = null,
    val author: String? = null,
    val illustrator: String? = null,
    val description: String? = null,
    val publisher: String? = null,
    val status: String = NovelStatusEnum.SERIALIZING.name,
    val totalVolumes: Int = 0,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val volumes: List<NovelVolume> = emptyList(),
    override val additions: List<AdditionalInfo> = emptyList(),
) : AdditionInterface {
    val bgmJson: JsonObject? get() = NovelAdditionType.BgmJson.valueOf(additions)
    val posterId: String? get() = NovelAdditionType.PosterId.valueOf(additions)
}

/**
 * 轻小说卷 DTO
 *
 * @property id 卷 ID
 * @property novelId 关联的小说 ID
 * @property volumeNumber 卷号（支持第 1 卷、第 1.5 卷短篇等）
 * @property name 卷标题
 * @property description 卷简介
 * @property releaseDate 出版日期
 * @property isbn ISBN 编号
 * @property bgmId Bangumi 关联 ID
 * @property additions 附加信息列表
 */
@Serializable
data class NovelVolume(
    val id: ULong,
    val novelId: ULong,
    val volumeNumber: Double,
    val name: String,
    val description: String? = null,
    val releaseDate: LocalDate? = null,
    val isbn: String? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    override val additions: List<AdditionalInfo> = emptyList(),
) : AdditionInterface {
    val bgmJson: JsonObject? get() = NovelAdditionType.BgmJson.valueOf(additions)
    val posterId: String? get() = NovelAdditionType.PosterId.valueOf(additions)
    val readingStatus: String get() = NovelAdditionType.ReadingStatus.valueOf(additions) ?: ReadingStatusEnum.UNREAD.name
    val fileStatus: String get() = NovelAdditionType.FileStatus.valueOf(additions) ?: ""
    val epubFilePath: String? get() = NovelAdditionType.EpubFilePath.valueOf(additions)
}

/**
 * 创建/更新轻小说请求体
 */
@Serializable
data class NovelCreateOrUpdateDto(
    val id: ULong? = null,
    val name: String,
    val originalName: String? = null,
    val author: String? = null,
    val illustrator: String? = null,
    val description: String? = null,
    val publisher: String? = null,
    val status: String = NovelStatusEnum.SERIALIZING.name,
    val totalVolumes: Int = 0,
    val bgmId: ULong? = null,
)

/**
 * 创建/更新轻小说卷请求体
 */
@Serializable
data class NovelVolumeCreateOrUpdateDto(
    val id: ULong? = null,
    val novelId: ULong,
    val volumeNumber: Double,
    val name: String,
    val description: String? = null,
    val releaseDate: LocalDate? = null,
    val isbn: String? = null,
    val bgmId: ULong? = null,
    val readingStatus: String? = null,
)
