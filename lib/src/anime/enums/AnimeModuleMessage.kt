package dev.sunriseydy.acgn.anime.enums

import dev.sunriseydy.acgn.base.interfaces.AnimeModule
import dev.sunriseydy.acgn.base.interfaces.ErrorMessage

/**
 * @author SunriseYDY
 * @date 2025-04-21 11:04
 */
enum class AnimeModuleError : AnimeModule, ErrorMessage {
    TARGET_DIR_EXISTS,
}