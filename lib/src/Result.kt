package dev.sunriseydy.acgn

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
)
