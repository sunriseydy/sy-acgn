package dev.sunriseydy.acgn.anime.enums

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.interfaces.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * 关联类型
 */
enum class AnimeAssociatedType : AnimeModule, AssociatedTypeInterface {
    ANIME, ANIME_SEASON, ANIME_EPISODE, ANIME_MOVIE,
}

/**
 * 附加类型
 */
object AnimeAdditionType {
    object TmdbJson : AnimeModule, AdditionTypeInterface {
        override val valueOf: (List<AdditionalInfo>) -> JsonObject? =
            { this.stringValueOf(it)?.let { Json.parseToJsonElement(it).jsonObject } }
    }

    object DownloadStatus : AnimeModule, StatusAdditionType
    object FileStatus : AnimeModule, StatusAdditionType
    object WatchStatus : AnimeModule, StatusAdditionType
}

enum class AnimeMonthType(val months: List<Int>) : AnimeModule, EnumKey {
    WINTER(listOf(1, 2, 3)), SPRING(listOf(4, 5, 6)), SUMMER(listOf(7, 8, 9)), FALL(listOf(10, 11, 12)),
}