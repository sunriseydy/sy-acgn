@file:UseSerializers(OffsetDateTimeSerializer::class)
package dev.sunriseydy.acgn.anime.dto

import dev.sunriseydy.acgn.OffsetDateTimeSerializer
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.base.enums.Status
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject
import java.time.OffsetDateTime

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
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val animeSeasons: List<AnimeSeason> = emptyList(),
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
    var downloadStatus: String = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var fileStatus: String = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var watchStatus: String = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

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
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val anime: Anime? = null,
    val animeEpisodes: List<AnimeEpisode> = emptyList(),
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
    var bgmJson: JsonObject? = AnimeAdditionType.BgmJson.valueOf(additions)
    var downloadStatus: String = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var fileStatus: String = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var watchStatus: String = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

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
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
    var downloadStatus: String = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var fileStatus: String = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var watchStatus: String = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

@Serializable
data class AnimeMovie(
    val id: ULong,
    val name: String,
    val description: String? = null,
    val releaseDate: LocalDate? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
    var downloadStatus: String = AnimeAdditionType.DownloadStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var fileStatus: String = AnimeAdditionType.FileStatus.valueOf(additions) ?: Status.UNPROCESS.key
    var watchStatus: String = AnimeAdditionType.WatchStatus.valueOf(additions) ?: Status.UNPROCESS.key
}

@Serializable
data class AnimeSeasonFile(
    val id: ULong,
    val path: String,
    val isDeleteSource: Boolean = false,
    val isDeleteTarget: Boolean = false,
)