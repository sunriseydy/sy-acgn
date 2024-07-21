package dev.sunriseydy.acgn.anime.dto

import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Anime(
    val id: ULong,
    val name: String,
    val originalName: String? = null,
    val description: String? = null,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val animeSeasons: List<AnimeSeason> = emptyList(),
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
}

@Serializable
data class AnimeSeason(
    val id: ULong,
    val animeId: ULong,
    val name: String,
    val originalName: String? = null,
    val description: String? = null,
    val season: Int,
    val year: Int,
    val month: Int,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val anime: Anime? = null,
    val animeEpisodes: List<AnimeEpisode> = emptyList(),
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
}

@Serializable
data class AnimeEpisode(
    val id: ULong,
    val animeId: ULong,
    val animeSeasonId: ULong,
    val name: String,
    val originalName: String? = null,
    val description: String? = null,
    val episode: Int,
    val publishedAt: Instant? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
}

@Serializable
data class AnimeMovie(
    val id: ULong,
    val name: String,
    val originalName: String? = null,
    val description: String? = null,
    val publishedAt: Instant? = null,
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val additions: List<AdditionalInfo> = emptyList(),
) {
    var tmdbJson: JsonObject? = AnimeAdditionType.TmdbJson.valueOf(additions)
}
