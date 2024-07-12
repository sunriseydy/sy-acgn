package dev.sunriseydy.acgn.anime.enums

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeAddition
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeMovie
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.interfaces.AdditionTypeInterface
import dev.sunriseydy.acgn.interfaces.AnimeModuleLocalizable
import dev.sunriseydy.acgn.interfaces.EnumLocalizable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * 关联类型
 */
enum class AnimeAssociatedType : AnimeModuleLocalizable, EnumLocalizable {
    ANIME, ANIME_SEASON, ANIME_EPISODE, ANIME_MOVIE,
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
enum class AnimeAdditionType : AnimeModuleLocalizable, AdditionTypeInterface<AnimeAddition> {
    TMDB_ID {
        override val valueOf: (List<AnimeAddition>) -> ULong? = { this.stringValueOf(it)?.toULong() }
    },
    TMDB_JSON {
        override val valueOf: (List<AnimeAddition>) -> JsonObject? =
            { this.stringValueOf(it)?.let { Json.parseToJsonElement(it).jsonObject } }
    }
    ;

    override val stringValueOf: (List<AnimeAddition>) -> String? = {
        it.find { addition -> addition.additionalType == this }?.value
    }
}