package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.enums.Localizable
import io.ktor.server.config.yaml.YamlConfig


/**
 * @author SunriseYDY
 * @date 2024-07-09 14:56
 */

object LocalizationTool {
    internal val DEFAULT_LANGUAGE = Language.SIMPLIFIED_CHINESE

    fun getLocalizationMessage(key: String): String {
        return this.getLocalizationMessage(DEFAULT_LANGUAGE, key)
    }

    fun getLocalizationMessage(language: Language, key: String): String {
        return this.getLocalizationYaml(language)
            ?.propertyOrNull(key)
            ?.getString()
            ?: key
    }

    fun getKeyFromEnum(enum: Enum<*>): String {
        if (enum is Localizable) {
            return "enum.${enum.moduleName.name}.${enum::class.simpleName}.${enum.name}"
        } else {
            throw IllegalArgumentException("$enum is not a Localizable enum")
        }
    }

    private fun getLocalizationYaml(language: Language) = YamlConfig("localization/${language.code}.yaml")
}
