package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.interfaces.AppConfigInterface
import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-12 22:29
 */
object CommonModuleAppConfig {

    object AppName : CommonModuleAppConfigInterface {
        override val configValue: String get() = this.stringValue ?: "SY ACGN"
    }

    object AppServer : CommonModuleAppConfigInterface {
        override val configValue: String? get() = this.stringValue
    }

    object AppLanguage : CommonModuleAppConfigInterface {
        override val configValue: Language
            get() = if (this.stringValue == null) {
                LocalizationTool.DEFAULT_LANGUAGE
            } else {
                Language.valueOf(this.stringValue!!)
            }
    }
}

interface CommonModuleAppConfigInterface : AppConfigInterface, CommonModuleLocalizable