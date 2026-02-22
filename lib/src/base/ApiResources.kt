package dev.sunriseydy.acgn.base

import io.ktor.resources.*
import kotlinx.serialization.Serializable

/**
 * API 根路由资源
 *
 * 所有 API 路由的父级资源，对应路径 `/api`。
 *
 * @author SunriseYDY
 * @date 2024-07-11
 */
@Serializable
@Resource("/api")
class ApiResource {
    /**
     * 错误路由资源，对应路径 `/api/error`
     */
    @Serializable
    @Resource("error")
    class Error(val parent: ApiResource = ApiResource())
}