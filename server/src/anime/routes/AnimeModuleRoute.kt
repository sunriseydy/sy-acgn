package dev.sunriseydy.acgn.anime.routes

import io.ktor.server.routing.*

fun Routing.configureAnimeModuleRoutes() {
    route("/anime") {
        rssRoutes()
        animeRoutes()
    }
}