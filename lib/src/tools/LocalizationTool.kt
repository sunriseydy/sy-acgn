package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.enums.Localizable
import dev.sunriseydy.acgn.exception.LocalizableException


/**
 * @author SunriseYDY
 * @date 2024-07-09 14:56
 */
object LocalizationTool {
    val DEFAULT_LANGUAGE = Language.SIMPLIFIED_CHINESE

    val localizations = mutableMapOf<String, String>()

    fun getLocalizationMessage(key: String, defaultValue: String = key): String {
        return localizations.getOrDefault(key, defaultValue)
    }

    fun getLocalizationMessage(enum: Enum<*>, defaultValue: String = getKeyFromEnum(enum)): String {
        return getLocalizationMessage(getKeyFromEnum(enum), defaultValue)
    }

    fun getLocalizationMessage(
        exception: LocalizableException,
        defaultValue: String = getKeyFromException(exception)
    ): String {
        return getLocalizationMessage(getKeyFromException(exception), defaultValue)
    }

    fun getKeyFromEnum(enum: Enum<*>): String {
        if (enum is Localizable) {
            return "enum.${enum.moduleName.name}.${enum::class.simpleName}.${enum.name}"
        } else {
            throw IllegalArgumentException("$enum is not a Localizable enum")
        }
    }

    fun getKeyFromException(exception: LocalizableException): String {
        return "message.error.${exception.moduleName}.${exception.exceptionCode}"
    }
}
