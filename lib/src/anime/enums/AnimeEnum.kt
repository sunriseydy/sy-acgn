package dev.sunriseydy.acgn.anime.enums

import dev.sunriseydy.acgn.base.interfaces.*

/**
 * Anime 关联类型枚举
 *
 * 定义了动画模块中的各类实体关联类型。
 */
enum class AnimeAssociatedType : AnimeModule, AssociatedTypeInterface {
    ANIME, ANIME_SEASON, ANIME_EPISODE, ANIME_MOVIE,
}

/**
 * Anime 附加类型
 *
 * 定义了动画模块中各种附加数据类型，包括元数据 JSON 和状态信息。
 */
object AnimeAdditionType {

    /** TMDB 元数据 JSON */
    object TmdbJson : AnimeModule, JsonObjectAdditionTypeInterface

    /** Bangumi 元数据 JSON */
    object BgmJson : AnimeModule, JsonObjectAdditionTypeInterface

    /** 下载状态 */
    object DownloadStatus : AnimeModule, StringAdditionType

    /** 文件状态 */
    object FileStatus : AnimeModule, StringAdditionType

    /** 观看状态 */
    object WatchStatus : AnimeModule, StringAdditionType

    /** 封面图片id */
    object PosterId : AnimeModule, StringAdditionType
}

/**
 * 动画季度月份类型
 *
 * @property months 包含的月份列表
 */
enum class AnimeMonthType(val months: List<Int>) : AnimeModule, EnumKey {
    /** 冬季（1-3月） */
    WINTER(listOf(1, 2, 3)),
    /** 春季（4-6月） */
    SPRING(listOf(4, 5, 6)),
    /** 夏季（7-9月） */
    SUMMER(listOf(7, 8, 9)),
    /** 秋季（10-12月） */
    FALL(listOf(10, 11, 12)),
}