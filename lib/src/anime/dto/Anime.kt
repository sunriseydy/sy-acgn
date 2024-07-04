package dev.sunriseydy.acgn.anime.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

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
    val associatedId: ULong,
    val associatedType: AnimeAssociatedType,
    val additionalType: AnimeAdditionType,
    val value: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

enum class AnimeAssociatedType() {
    ANIME, ANIME_SEASON, ANIME_EPISODE, ANIME_MOVIE,
}

fun AnimeAssociatedType(obj: Any) = when (obj) {
    is Anime -> AnimeAssociatedType.ANIME
    is AnimeSeason -> AnimeAssociatedType.ANIME_SEASON
    is AnimeEpisode -> AnimeAssociatedType.ANIME_EPISODE
    is AnimeMovie -> AnimeAssociatedType.ANIME_MOVIE
    else -> throw IllegalArgumentException("Invalid AnimeAssociatedType")
}

enum class AnimeAdditionType() {
    TMDB_ID {
        override fun getValue(animeAdditions: List<AnimeAddition>): ULong? = getStringValue(animeAdditions)?.toULong()
    },
    TMDB_JSON {
        override fun getValue(animeAdditions: List<AnimeAddition>): JsonObject? = getStringValue(animeAdditions)?.let {
            Json.parseToJsonElement(it).jsonObject
        }
    }
    ;

    fun getStringValue(animeAdditions: List<AnimeAddition>): String? {
        return animeAdditions.find { it.additionalType == this }?.value
    }

    abstract fun getValue(animeAdditions: List<AnimeAddition>): Any?
}