package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.interfaces.AppConfigInterface
import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable

/**
 * @author SunriseYDY
 * @date 2024-07-12 22:29
 */
object CommonModuleAppConfig {
    object AppName : CommonModuleAppConfigInterface {
        override val configValue: String get() = this.stringValue ?: "SY ACGN"
    }

    object AppServer : CommonModuleAppConfigInterface {
        override val configValue: String get() = this.stringValue ?: "http://localhost:9390"
    }
}

interface CommonModuleAppConfigInterface : AppConfigInterface, CommonModuleLocalizable