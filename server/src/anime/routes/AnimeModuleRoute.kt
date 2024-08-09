package dev.sunriseydy.acgn.server.anime.routes

import io.ktor.server.routing.*

fun Routing.configureAnimeModuleRoutes() {
    route("/anime") {
        rssRoutes()
        animeRoutes()
    }
}