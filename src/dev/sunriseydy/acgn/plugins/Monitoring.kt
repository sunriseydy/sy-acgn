package dev.sunriseydy.acgn.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.*
import java.util.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        callIdMdc("call-id")
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CallId) {
        generate { UUID.randomUUID().toString().replace("-", "") }
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }
}
