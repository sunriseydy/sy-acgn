package dev.sunriseydy.acgn.exception

import dev.sunriseydy.acgn.interfaces.ErrorMessage

/**
 * @author SunriseYDY
 * @date 2024-07-11 18:20
 */
class MessageException(val errorMessage: ErrorMessage, cause: Throwable? = null) :
    RuntimeException(errorMessage.key, cause) {
    override val message get() = errorMessage.meaning
}