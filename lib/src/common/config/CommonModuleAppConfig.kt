package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.interfaces.AppConfigInterface
import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable

/**
 * @author SunriseYDY
 * @date 2024-07-12 22:29
 */
object CommonModuleAppConfig {
    object AppName : AppConfigInterface, CommonModuleLocalizable {
        override val configValue: String get() = this.stringValue ?: "SY ACGN"
    }
}