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
        exception<Throwable> { call, cause -> handleError(call, cause) }
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

/**
 * 全局异常处理器
 *
 * 捕获所有未处理的异常，记录错误日志，并将其包装为 Result<Unit> 对象返回给客户端。
 * 确保客户端始终收到格式统一的 JSON 响应，即使在服务器出错时也是如此。
 */
suspend fun handleError(call: ApplicationCall, cause: Throwable) {
    call.application.log.error("exception", cause)
    call.respond(
        HttpStatusCode.OK,
        Result<Unit>(failed = true, message = cause.message ?: cause.toString())
    )
}
