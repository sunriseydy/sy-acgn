package dev.sunriseydy.acgn.anime.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.service.AnimeService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * @author SunriseYDY
 * @date 2024-07-15 14:45
 */
fun Route.animeRoutes(animeService: AnimeService = AnimeService()) {
    route("/anime") {
        get("/name-id") {
            call.respond(Result(data = animeService.getAnimeNameAndId()))
        }
    }
}