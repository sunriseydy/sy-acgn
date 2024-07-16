package dev.sunriseydy.acgn.anime.enums

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
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

/**
 * 附加类型
 */
object AnimeAdditionType {
    object tmdbJson : AnimeModuleLocalizable, AdditionTypeInterface {
        override val valueOf: (List<AdditionalInfo>) -> JsonObject? =
            { this.stringValueOf(it)?.let { Json.parseToJsonElement(it).jsonObject } }
    }
}