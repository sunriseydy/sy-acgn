package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.base.enums.Language


/**
 * @author SunriseYDY
 * @date 2024-07-09 14:56
 */
object LocalizationTool {
    val DEFAULT_LANGUAGE = Language.SIMPLIFIED_CHINESE
    var currentLanguage = DEFAULT_LANGUAGE

    private val localizations = mutableMapOf<String, String>()

    fun putAll(localizations: Map<String, String>) = this.localizations.putAll(localizations)

    fun putLocalization(key: String, value: String) {
        localizations[key] = value
    }

    fun getLocalizations() = localizations

    fun getLocalization(key: String, defaultValue: String = key): String {
        return localizations.getOrElse(key) {
            println("the key [$key] on current language [${currentLanguage.originName}] doesn't have a localization")
            defaultValue
        }
    }
}

fun i(key: String, defaultValue: String = key) = LocalizationTool.getLocalization(key, defaultValue)