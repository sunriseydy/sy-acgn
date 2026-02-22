package dev.sunriseydy.acgn.server.base.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level
import java.util.*

/**
 * 配置日志监控
 *
 * 安装 CallLogging 和 CallId 插件：
 * - CallLogging：记录请求日志，MDC 中包含 call-id
 * - CallId：从 X-Request-Id 获取或自动生成请求 ID，并回写到响应头
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        callIdMdc("call-id")
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString().replace("-", "") }
        replyToHeader(HttpHeaders.XRequestId)
    }
}
