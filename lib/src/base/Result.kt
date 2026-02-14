package dev.sunriseydy.acgn.base

import kotlinx.serialization.Serializable

/**
 * 通用响应结果封装类
 *
 * 用于封装 API 或业务逻辑的执行结果，包含成功标志、消息和数据。
 *
 * @param failed 是否失败标志，默认为 false（成功）
 * @param message 结果消息，默认为 "success"，失败时通常包含错误描述
 * @param data 结果数据，可能为空
 *
 * @author SunriseYDY
 * @date 2024-07-11 22:48
 */
@Serializable
data class Result<T>(
    val failed: Boolean = false,
    val message: String = "success",
    val data: T? = null
) {
    /**
     * 检查是否成功
     *
     * 如果失败则抛出 IllegalStateException，否则返回数据（可能为空）。
     */
    fun checkSuccess(): T? = if (failed) throw error(message) else data

    /**
     * 检查是否成功且数据不为空
     *
     * 如果失败则抛出 IllegalStateException，如果数据为空则抛出 IllegalStateException，
     * 否则返回非空数据。
     */
    fun checkSuccessAndNotNull(): T = if (failed) throw error(message) else checkNotNull(data)
}