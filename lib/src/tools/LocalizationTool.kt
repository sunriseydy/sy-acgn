package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.exception.LocalizableException
import dev.sunriseydy.acgn.interfaces.EnumLocalizable


/**
 * @author SunriseYDY
 * @date 2024-07-09 14:56
 */
object LocalizationTool {
    val DEFAULT_LANGUAGE = Language.SIMPLIFIED_CHINESE

    val localizations = mutableMapOf<String, String>()

    fun getLocalization(key: String, defaultValue: String = key): String {
        return localizations.getOrDefault(key, defaultValue)
    }

    fun getLocalizationKeyFromEnum(enum: EnumLocalizable): String {
        if (enum is Enum<*>) {
            return "enum.${enum.moduleName.name}.${enum::class.simpleName}.${enum.name}"
        } else {
            throw IllegalArgumentException("$enum is not a Localizable enum")
        }
    }

    fun getLocalizationKeyFromException(exception: LocalizableException): String {
        return "message.error.${exception.moduleName}.${exception.exceptionCode}"
    }
}
