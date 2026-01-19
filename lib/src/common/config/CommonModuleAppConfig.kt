package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.base.interfaces.AppConfigInterface
import dev.sunriseydy.acgn.base.interfaces.CommonModule
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-12 22:29
 */
object CommonModuleAppConfig {

    object AppName : AppConfigInterface, CommonModule {
        override val configValue: String get() = this.stringValue ?: "SY ACGN"
    }

    object AppServer : AppConfigInterface, CommonModule {
        override val configValue: String? get() = this.stringValue
    }

    object AppLanguage : AppConfigInterface, CommonModule {
        override val configValue: Language
            get() = if (this.stringValue == null) {
                LocalizationTool.DEFAULT_LANGUAGE
            } else {
                Language.valueOf(this.stringValue!!)
            }
    }
}