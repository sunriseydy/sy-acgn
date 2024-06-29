package dev.sunriseydy.acgn.anime.routes

import io.ktor.server.routing.*

fun Route.configureAnimeModuleRoutes() {
    route("/anime") {
        rssRoutes()
    }
}