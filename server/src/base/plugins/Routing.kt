package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.base.ApiResource
import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.base.exception.MessageException
import dev.sunriseydy.acgn.common.enums.CommonModuleError
import dev.sunriseydy.acgn.server.anime.routes.configureAnimeModuleRoutes
import dev.sunriseydy.acgn.server.common.routes.configureCommonModuleRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * 配置应用路由
 *
 * 安装全局异常处理 (StatusPages) 和资源插件 (Resources)。
 * 定义静态资源路径和测试路由。
 * 注册 Common 模块和 Anime 模块的路由。
 */
fun Application.configureRouting() {
    install(StatusPages) {
        // 业务异常返回 200 + Result(failed=true)，方便客户端统一处理
        exception<MessageException> { call, cause ->
            call.application.log.warn("业务异常: ${cause.message}")
            call.respond(
                HttpStatusCode.OK,
                Result<Unit>(failed = true, message = cause.message ?: cause.toString())
            )
        }
        // 参数异常返回 400
        exception<IllegalArgumentException> { call, cause ->
            call.application.log.warn("参数异常: ${cause.message}")
            call.respond(
                HttpStatusCode.OK,
                Result<Unit>(failed = true, message = cause.message ?: "请求参数错误")
            )
        }
        // 未找到资源返回 404
        exception<NoSuchElementException> { call, cause ->
            call.application.log.warn("资源未找到: ${cause.message}")
            call.respond(
                HttpStatusCode.OK,
                Result<Unit>(failed = true, message = cause.message ?: "资源未找到")
            )
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.application.log.warn("资源未找到: ${call.request.uri}")
            call.respond(
                HttpStatusCode.OK,
                Result<Unit>(failed = true, message = "资源未找到: ${call.request.uri}")
            )
        }
        // 其他未知异常返回 500
        exception<Throwable> { call, cause ->
            call.application.log.error("服务器内部错误", cause)
            call.respond(
                HttpStatusCode.OK,
                Result<Unit>(failed = true, message = cause.message ?: cause.toString())
            )
        }
    }
    install(Resources)
    routing {
        staticResources("/resources", "static")
        get<ApiResource> {
            call.respond(Pair("SY ACGN", "Hello, World!"))
        }
        get<ApiResource.Error> {
            throw MessageException(CommonModuleError.TEST)
        }
        configureCommonModuleRoutes()
        configureAnimeModuleRoutes()
    }
}
