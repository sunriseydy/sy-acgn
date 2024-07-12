package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.interfaces.CommonModuleAppConfigInterface

/**
 * @author SunriseYDY
 * @date 2024-07-12 22:29
 */
object CommonModuleAppConfig {
    object AppName : CommonModuleAppConfigInterface<String> {
        override val configValue: String get() = this.stringValue ?: "SY ACGN"
    }
}