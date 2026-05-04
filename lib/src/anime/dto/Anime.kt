package dev.sunriseydy.acgn.anime.dto

import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.base.enums.Status
import dev.sunriseydy.acgn.base.interfaces.AdditionInterface
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
/**
 * 动画实体 DTO
 *
 * @property id 动画 ID
 * @property name 动画名称
 * @property tmdbId TMDB 关联 ID
 * @property bgmId Bangumi 关联 ID
 * @property animeSeasons 关联的动画季度列表
 * @property additions 附加信息列表
 */
@Serializable
data class Anime(
    val id: ULong,
    val name: String,
    val description: String? = null,
    val firstAirDate: LocalDate? = null,
    val lastAirDate: LocalDate? = null,
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val animeSeasons: List<AnimeSeason> = emptyList(),
    override val additions: List<AdditionalInfo> = emptyList(),
    ) : AdditionInterface {
    val tmdbJson: JsonObject? get() = AnimeAdditionType.TmdbJson.valueOf(additions)
    val downloadStatus: String get() = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val fileStatus: String get() = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val watchStatus: String get() = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

/**
 * 动画季度 DTO
 *
 * @property id 季度 ID
 * @property animeId 关联的动画 ID
 * @property season 季度编号
 * @property year 年份
 * @property month 月份
 */
@Serializable
data class AnimeSeason(
    val id: ULong,
    val animeId: ULong,
    val name: String,
    val description: String? = null,
    val season: Int,
    val numberOfEpisodes: Int = 0,
    val year: Int,
    val month: Int,
    val airDate: LocalDate? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val anime: Anime? = null,
    val animeEpisodes: List<AnimeEpisode> = emptyList(),
    override val additions: List<AdditionalInfo> = emptyList(),
) : AdditionInterface {
    val tmdbJson: JsonObject? get() = AnimeAdditionType.TmdbJson.valueOf(additions)
    val bgmJson: JsonObject? get() = AnimeAdditionType.BgmJson.valueOf(additions)
    val downloadStatus: String get() = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val fileStatus: String get() = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val watchStatus: String get() = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

/**
 * 动画剧集 DTO
 *
 * @property id 剧集 ID
 * @property animeId 关联的动画 ID
 * @property animeSeasonId 关联的季度 ID
 * @property episode 集数
 */
@Serializable
data class AnimeEpisode(
    val id: ULong,
    val animeId: ULong,
    val animeSeasonId: ULong,
    val name: String,
    val description: String? = null,
    val episode: Int,
    val airDate: LocalDate? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    override val additions: List<AdditionalInfo> = emptyList(),
) : AdditionInterface {
    val tmdbJson: JsonObject? get() = AnimeAdditionType.TmdbJson.valueOf(additions)
    val downloadStatus: String get() = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val fileStatus: String get() = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val watchStatus: String get() = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

/**
 * 动画电影 DTO
 *
 * @property id 电影 ID
 * @property name 电影名称
 * @property releaseDate 上映日期
 */
@Serializable
data class AnimeMovie(
    val id: ULong,
    val name: String,
    val description: String? = null,
    val releaseDate: LocalDate? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    override val additions: List<AdditionalInfo> = emptyList(),
) : AdditionInterface {
    val tmdbJson: JsonObject? get() = AnimeAdditionType.TmdbJson.valueOf(additions)
    val downloadStatus: String get() = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val fileStatus: String get() = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    val watchStatus: String get() = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

/**
 * 动画季度文件 DTO
 *
 * @property id 文件 ID
 * @property path 文件路径
 * @property isDeleteSource 是否删除源目录
 * @property isDeleteTarget 是否删除目标目录（如果存在）
 * @property episodeOffset 集数偏移量
 */
@Serializable
data class AnimeSeasonFile(
    val id: ULong,
    val path: String,
    val isDeleteSource: Boolean = false,
    val isDeleteTarget: Boolean = false,
    val episodeOffset: Int? = 0,
)