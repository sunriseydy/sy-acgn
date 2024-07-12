package dev.sunriseydy.acgn.interfaces

import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-12 15:07
 */
interface AppConfigInterface : Localizable {
    fun getConfigKey(): String = "config.${this.moduleName}.${if (this is Enum<*>) this.name else null}"

    fun getConfigMeaningKey(): String = LocalizationTool.getLocalizationMessage(this.getConfigKey() + ".meaning")

    fun getConfigDescriptionKey(): String =
        LocalizationTool.getLocalizationMessage(this.getConfigKey() + ".description")

    fun getConfigStringValue(): String? = AppConfigTool.appConfig[this.getConfigKey()]

    fun getConfigValue(): Any?
}

interface CommonModuleAppConfigInterface : AppConfigInterface, CommonModuleLocalizable
interface AnimeModuleAppConfigInterface : AppConfigInterface, AnimeModuleLocalizable
