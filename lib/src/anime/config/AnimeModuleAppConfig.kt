package dev.sunriseydy.acgn.anime.config

import dev.sunriseydy.acgn.base.interfaces.AnimeModule
import dev.sunriseydy.acgn.base.interfaces.AppConfigInterface
import dev.sunriseydy.acgn.base.interfaces.StringAppConfig

/**
 * Anime 模块配置常量
 *
 * 定义了动画模块中需要的各项配置项。
 * 所有配置均为字符串类型，通过 [AppConfigInterface] 统一管理。
 *
 * @author SunriseYDY
 * @date 2024-07-16 20:03
 */
object AnimeModuleAppConfig {

    /** TMDB API 密钥 */
    object TmdbApiKey : StringAppConfig, AnimeModule

    /** 媒体目标目录 */
    object MediaTargetDirectory : StringAppConfig, AnimeModule

    /** Bangumi User-Agent */
    object BgmUserAgent : StringAppConfig, AnimeModule
}