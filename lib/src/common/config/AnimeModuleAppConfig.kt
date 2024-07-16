package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.interfaces.AnimeModuleLocalizable
import dev.sunriseydy.acgn.interfaces.AppConfigInterface

/**
 * @author SunriseYDY
 * @date 2024-07-16 20:03
 */
object AnimeModuleAppConfig {
    object TmdbApiKey : AppConfigInterface, AnimeModuleLocalizable {
        override val configValue: String? get() = this.stringValue
    }
}