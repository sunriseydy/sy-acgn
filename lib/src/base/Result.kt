package dev.sunriseydy.acgn.base

import kotlinx.serialization.Serializable

/**
 * @author SunriseYDY
 * @date 2024-07-11 22:48
 */
@Serializable
data class Result<T>(
    val failed: Boolean = false,
    val message: String = "success",
    val data: T? = null
) {
    fun checkSuccess(): T? = if (failed) throw error(message) else data
    fun checkSuccessAndNotNull(): T = if (failed) throw error(message) else checkNotNull(data)
}