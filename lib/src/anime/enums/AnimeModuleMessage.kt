package dev.sunriseydy.acgn.anime.enums

import dev.sunriseydy.acgn.interfaces.AnimeModule
import dev.sunriseydy.acgn.interfaces.ErrorMessage

/**
 * @author SunriseYDY
 * @date 2025-04-21 11:04
 */
enum class AnimeModuleError : AnimeModule, ErrorMessage {
    QB_LOGIN_FAILED,
    QB_REQUEST_FAILED,
    QB_DOWNLOAD_TORRENT_FAILED,
    QB_PARSE_TORRENT_FAILED,
    QB_PARSE_MAGNET_FAILED,
    QB_PARSE_HASH_FAILED,
    TARGET_DIR_EXISTS,
}