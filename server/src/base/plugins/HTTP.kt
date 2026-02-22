package dev.sunriseydy.acgn.server.base.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.forwardedheaders.*

/**
 * 配置 HTTP 服务器特性
 *
 * 安装转发头解析（反向代理支持）和 CORS 跨域配置。
 * 注意：ForwardedHeaders/XForwardedHeaders 仅在反向代理后使用，直接暴露时存在安全风险。
 */
fun Application.configureHTTP() {
    install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
        allowCredentials = true
    }
}
