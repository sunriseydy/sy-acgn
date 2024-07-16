package dev.sunriseydy.acgn.anime.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.service.AnimeService
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * @author SunriseYDY
 * @date 2024-07-15 14:45
 */
fun Route.animeRoutes(animeService: AnimeService = AnimeService()) {
    route("/anime") {
        get("/name-id-map") {
            call.respond(Result(data = animeService.getAnimeNameAndId(call.parameters["name"])))
        }
        delete("/{animeId}") {
            val animeId =
                call.parameters["animeId"]!!.toULong()
            call.respond(Result(data = animeService.removeAnimeById(animeId)))
        }
        route("/season") {
            get("/by-anime-id/{animeId}") {
                val animeId =
                    call.parameters["animeId"]!!.toULong()
                call.respond(Result(data = animeService.getAnimeSeasonByAnimeId(animeId)))
            }
            post {
                call.respond(Result(data = animeService.saveAnimeSeason(call.receive())))
            }
            delete("/{seasonId}") {
                val seasonId =
                    call.parameters["seasonId"]!!.toULong()
                call.respond(Result(data = animeService.removeAnimeSeasonById(seasonId)))
            }

            route("/episode") {
                delete("/{episodeId}") {
                    val episodeId =
                        call.parameters["episodeId"]!!.toULong()
                    call.respond(Result(data = animeService.removeAnimeEpisodeById(episodeId)))
                }
            }
        }
    }
}