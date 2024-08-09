package dev.sunriseydy.acgn.server.plugins

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.exception.CommonModuleException
import dev.sunriseydy.acgn.server.anime.routes.configureAnimeModuleRoutes
import dev.sunriseydy.acgn.server.common.routes.configureCommonModuleRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
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
        staticResources("/resources", "static")
        route("/api") {
            get {
                call.respond(Pair("SY ACGN", "Hello, World!"))
            }
            get("/error") {
                throw CommonModuleException("test")
            }
            configureCommonModuleRoutes()
            configureAnimeModuleRoutes()
        }
    }
}

suspend fun handleError(call: ApplicationCall, cause: Throwable) {
    call.application.log.error("exception", cause)
    call.respond(
        HttpStatusCode.InternalServerError,
        Result<Unit>(failed = true, message = cause.message ?: cause.toString())
    )
}
