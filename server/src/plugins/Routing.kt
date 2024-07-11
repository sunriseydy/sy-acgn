package dev.sunriseydy.acgn.plugins

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.routes.configureAnimeModuleRoutes
import dev.sunriseydy.acgn.exception.CommonModuleException
import dev.sunriseydy.acgn.exception.LocalizableException
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause -> handleError(call, cause) }
    }
    install(Resources)
    routing {
        route("/api") {
            get {
                call.respond(Pair("SY ACGN", "Hello, World!"))
            }
            get("/error") {
                throw CommonModuleException("test")
            }
            configureAnimeModuleRoutes()
        }
    }
}

suspend fun handleError(call: ApplicationCall, cause: Throwable) {
    call.application.log.error("exception", cause)
    val message = if (cause is LocalizableException) {
        LocalizationTool.getLocalizationMessage(cause)
    } else {
        cause.message?: cause.toString()
    }
    call.respond(HttpStatusCode.InternalServerError, Result<Unit>(failed = true, message = message))
}
