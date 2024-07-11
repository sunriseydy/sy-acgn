package dev.sunriseydy.acgn.exception

import dev.sunriseydy.acgn.enums.AnimeLocalizable
import dev.sunriseydy.acgn.enums.CommonLocalizable
import dev.sunriseydy.acgn.enums.Localizable

/**
 * @author SunriseYDY
 * @date 2024-07-11 18:20
 */
abstract class LocalizableException : RuntimeException, Localizable {
    constructor(message: String?, cause: Throwable?)

    abstract val exceptionCode: String
}

class CommonModuleException(
    override val exceptionCode: String,
    override val cause: Throwable? = null
) : LocalizableException(exceptionCode, cause), CommonLocalizable

class AnimeModuleException(
    override val exceptionCode: String,
    override val cause: Throwable? = null
) : LocalizableException(exceptionCode, cause), AnimeLocalizable