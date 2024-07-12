package dev.sunriseydy.acgn.interfaces

import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-12 15:07
 */
interface AppConfigInterface<out T> : Localizable {
    fun getConfigKey(): String = "config.${this.moduleName}.${this::class.simpleName}"

    fun getConfigMeaningKey(): String = LocalizationTool.getLocalizationMessage(this.getConfigKey() + ".meaning")

    fun getConfigDescriptionKey(): String =
        LocalizationTool.getLocalizationMessage(this.getConfigKey() + ".description")

    fun getConfigStringValue(): String? = AppConfigTool.appConfig[this.getConfigKey()]

    fun getConfigValue(): T?
}

interface CommonModuleAppConfigInterface<T> : AppConfigInterface<T>, CommonModuleLocalizable
interface AnimeModuleAppConfigInterface<T> : AppConfigInterface<T>, AnimeModuleLocalizable
