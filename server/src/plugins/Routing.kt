package dev.sunriseydy.acgn.plugins

import dev.sunriseydy.acgn.anime.routes.configureAnimeModuleRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    install(Resources)
    routing {
        route("/api") {
            get {
                call.respond(Pair("SY ACGN", "Hello, World!"))
            }
            configureAnimeModuleRoutes()
        }
    }
}
