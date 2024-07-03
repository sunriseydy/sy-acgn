package dev.sunriseydy.acgn.anime.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
data class Anime(
    var id: ULong? = null,
    var name: String,
    var originalName: String? = null,
    var description: String? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    var animeSeasons: List<AnimeSeason> = emptyList()
    var additions: List<AnimeAddition> = emptyList()
    var tmdbId: ULong? = null
    var tmdbJson: JsonObject? = null
}

@Serializable
data class AnimeSeason(
    var id: ULong? = null,
    var animeId: ULong? = null,
    var name: String,
    var originalName: String? = null,
    var description: String? = null,
    var season: Int,
    var year: Int,
    var month: Int,
    var startedAt: Instant? = null,
    var endedAt: Instant? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    var animeEpisodes: List<AnimeEpisode> = emptyList()
    var additions: List<AnimeAddition> = emptyList()
}

@Serializable
data class AnimeEpisode(
    var id: ULong? = null,
    var animeId: ULong? = null,
    var animeSeasonId: ULong? = null,
    var name: String,
    var originalName: String? = null,
    var description: String? = null,
    var episode: Int,
    var publishedAt: Instant? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    var additions: List<AnimeAddition> = emptyList()
}

@Serializable
data class AnimeMovie(
    var id: ULong? = null,
    var name: String,
    var originalName: String? = null,
    var description: String? = null,
    var publishedAt: Instant? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    var additions: List<AnimeAddition> = emptyList()
}

@Serializable
data class AnimeAddition(
    var id: String? = null,
    var associatedId: ULong,
    var associatedType: AnimeAssociatedType,
    var additionalType: AnimeAdditionType,
    var value: String,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
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