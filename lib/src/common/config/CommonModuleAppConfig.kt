package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.interfaces.CommonModuleAppConfigInterface

/**
 * @author SunriseYDY
 * @date 2024-07-12 22:29
 */
object CommonModuleAppConfig {
    object AppName : CommonModuleAppConfigInterface<String> {
        override fun getConfigValue(): String = super.getConfigStringValue() ?: "SY ACGN"
    }
}