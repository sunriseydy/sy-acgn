package dev.sunriseydy.acgn.anime.config

import dev.sunriseydy.acgn.base.interfaces.AnimeModule
import dev.sunriseydy.acgn.base.interfaces.AppConfigInterface

/**
 * @author SunriseYDY
 * @date 2024-07-16 20:03
 */
object AnimeModuleAppConfig {
    object TmdbApiKey : AppConfigInterface, AnimeModule {
        override val configValue: String? get() = this.stringValue
    }

    object QbApiBaseUrl : AppConfigInterface, AnimeModule {
        override val configValue: String? get() = this.stringValue
    }

    object QbUserName : AppConfigInterface, AnimeModule {
        override val configValue: String? get() = this.stringValue
    }

    object QbPassword : AppConfigInterface, AnimeModule {
        override val configValue: String? get() = this.stringValue
    }

    object MediaTargetDirectory : AppConfigInterface, AnimeModule {
        override val configValue: String? get() = this.stringValue
    }
}