package dev.sunriseydy.acgn.anime.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.anime.service.AnimeService
import dev.sunriseydy.acgn.anime.tools.TmdbTool
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * @author SunriseYDY
 * @date 2024-07-15 14:45
 */
fun Routing.animeRoutes(animeService: AnimeService = AnimeService()) {
    route("/anime") {
        get("/name-id-map") {
            call.respond(Result(data = animeService.getAnimeNameAndId(call.parameters["name"])))
        }
        get("/cache") {
            call.respond(Result(data = animeService.getAllAnimeWithAdditionFromCache()))
        }
        get {
            call.respond(Result(data = animeService.getAllAnimeWithAdditionFromDB()))
        }
        put("/refresh") {
            call.respond(Result(data = animeService.refreshAnimeCache()))
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
                call.respond(Result(data = animeService.getAnimeSeasonWithAdditionByAnimeId(animeId)))
            }
            get("/by-year-and-month-type") {
                val year = call.parameters["year"]!!.toInt()
                val monthType = call.parameters["monthType"]!!.let {
                    AnimeMonthType.valueOf(it)
                }
                call.respond(Result(data = animeService.getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(year, monthType)))
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

        route("/tmdb") {
            get("/search-anime-tv") {
                val query = call.parameters["query"]!!
                call.respond(Result(data = TmdbTool().searchAnimeTV(query)))
            }
            get("/search-anime-movie") {
                val query = call.parameters["query"]!!
                call.respond(Result(data = TmdbTool().searchAnimeMovie(query)))
            }
            get("/tv-detail") {
                val id = call.parameters["id"]!!.toInt()
                call.respond(Result(data = TmdbTool().getTvDetails(id)))
            }
            get("/season-detail") {
                val id = call.parameters["id"]!!.toInt()
                val season = call.parameters["season"]!!.toInt()
                call.respond(Result(data = TmdbTool().getTvSeasonDetails(id, season)))
            }
            get("/movie-detail") {
                val id = call.parameters["id"]!!.toInt()
                call.respond(Result(data = TmdbTool().getMovieDetails(id)))
            }
        }
    }
}