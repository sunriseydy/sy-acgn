package dev.sunriseydy.acgn.server.anime.routes

import io.ktor.server.routing.*

fun Route.configureAnimeModuleRoutes() {
    route("/anime") {
        rssRoutes()
        animeRoutes()
    }
}