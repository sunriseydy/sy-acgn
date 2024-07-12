package dev.sunriseydy.acgn.common.enums

import dev.sunriseydy.acgn.interfaces.CommonModuleAppConfigInterface

/**
 * @author SunriseYDY
 * @date 2024-07-12 16:27
 */
enum class CommonModuleAppConfigEnum : CommonModuleAppConfigInterface {
    APP_NAME {
        override fun getConfigValue(): String? = this.getConfigStringValue()
    },
}