package dev.sunriseydy.acgn.anime.dto

import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Anime(
    val id: ULong,
    val name: String,
    val originalName: String? = null,
    val description: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    var animeSeasons: List<AnimeSeason> = emptyList()
    var additions: List<AnimeAddition> = emptyList()
    var tmdbId: ULong? = null
    var tmdbJson: JsonObject? = null
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
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    var animeEpisodes: List<AnimeEpisode> = emptyList()
    var additions: List<AnimeAddition> = emptyList()
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
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    var additions: List<AnimeAddition> = emptyList()
}

@Serializable
data class AnimeMovie(
    val id: ULong,
    val name: String,
    val originalName: String? = null,
    val description: String? = null,
    val publishedAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    var additions: List<AnimeAddition> = emptyList()
}

@Serializable
data class AnimeAddition(
    val id: String,
    /**
     * 关联id
     */
    val associatedId: ULong,
    /**
     * 关联类型
     */
    val associatedType: AnimeAssociatedType,
    /**
     * 附加类型
     */
    val additionalType: AnimeAdditionType,
    /**
     * 附加值
     */
    val value: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
