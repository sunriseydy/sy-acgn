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
    val tmdbId: ULong? = null,
    val bgmId: ULong? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    var animeSeasons: List<AnimeSeason> = emptyList()
    var additions: List<AdditionalInfo> = emptyList()
    var tmdbJson: JsonObject? = AnimeAdditionType.tmdbJson.valueOf(additions)
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
) {
    var anime: Anime? = null
    var animeEpisodes: List<AnimeEpisode> = emptyList()
    var additions: List<AdditionalInfo> = emptyList()
    var tmdbJson: JsonObject? = AnimeAdditionType.tmdbJson.valueOf(additions)
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
) {
    var additions: List<AdditionalInfo> = emptyList()
    var tmdbJson: JsonObject? = AnimeAdditionType.tmdbJson.valueOf(additions)
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
) {
    var additions: List<AdditionalInfo> = emptyList()
    var tmdbJson: JsonObject? = AnimeAdditionType.tmdbJson.valueOf(additions)
}
