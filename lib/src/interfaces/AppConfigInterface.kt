package dev.sunriseydy.acgn.interfaces

import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-12 15:07
 */
interface AppConfigInterface : Key {
    val configKey get() = "config.${this.moduleName}.${this::class.simpleName}"
    override val key get() = "${this.configKey}.meaning"
    val descriptionKey get() = "${this.configKey}.description"
    val description get() = LocalizationTool.getLocalization(this.descriptionKey)

    val stringValue: String? get() = AppConfigTool.getAppConfigStringValue(this.configKey)

    val configValue: Any?
}
