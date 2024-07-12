package dev.sunriseydy.acgn.exception

import dev.sunriseydy.acgn.interfaces.AnimeModuleLocalizable
import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable
import dev.sunriseydy.acgn.interfaces.Localizable
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-11 18:20
 */
abstract class LocalizableException(val exceptionCode: String, cause: Throwable? = null) :
    RuntimeException(exceptionCode, cause), Localizable {
    override val message get() = this.localization
    override val localizationKey get() = LocalizationTool.getLocalizationKeyFromException(this)
}

class CommonModuleException(
    exceptionCode: String,
    cause: Throwable? = null
) : LocalizableException(exceptionCode, cause), CommonModuleLocalizable

class AnimeModuleException(
    exceptionCode: String,
    cause: Throwable? = null
) : LocalizableException(exceptionCode, cause), AnimeModuleLocalizable