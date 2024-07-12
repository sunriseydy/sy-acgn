package dev.sunriseydy.acgn.exception

import dev.sunriseydy.acgn.enums.AnimeLocalizable
import dev.sunriseydy.acgn.enums.CommonLocalizable
import dev.sunriseydy.acgn.enums.Localizable
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-11 18:20
 */
abstract class LocalizableException(val exceptionCode: String, cause: Throwable? = null) :
    RuntimeException(exceptionCode, cause), Localizable {
    override val message: String
        get() = LocalizationTool.getLocalizationMessage(this)
}

class CommonModuleException(
    exceptionCode: String,
    cause: Throwable? = null
) : LocalizableException(exceptionCode, cause), CommonLocalizable

class AnimeModuleException(
    exceptionCode: String,
    cause: Throwable? = null
) : LocalizableException(exceptionCode, cause), AnimeLocalizable