package dev.sunriseydy.acgn.anime.enums

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeAddition
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeMovie
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.enums.Localizable
import dev.sunriseydy.acgn.enums.ModuleName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * 关联类型
 */
enum class AnimeAssociatedType : Localizable {
    ANIME, ANIME_SEASON, ANIME_EPISODE, ANIME_MOVIE,
    ;
    override val moduleName = ModuleName.ANIME
}

fun AnimeAssociatedType(obj: Any) = when (obj) {
    is Anime -> AnimeAssociatedType.ANIME
    is AnimeSeason -> AnimeAssociatedType.ANIME_SEASON
    is AnimeEpisode -> AnimeAssociatedType.ANIME_EPISODE
    is AnimeMovie -> AnimeAssociatedType.ANIME_MOVIE
    else -> throw IllegalArgumentException("Invalid AnimeAssociatedType")
}

/**
 * 附加类型
 */
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