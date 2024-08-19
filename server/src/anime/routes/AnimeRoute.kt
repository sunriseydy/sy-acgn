package dev.sunriseydy.acgn.server.anime.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.server.anime.service.AnimeService
import dev.sunriseydy.acgn.server.anime.tools.TmdbTool
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
        get("/name") {
            call.respond(Result(data = animeService.searchAnimeByName(call.parameters["name"])))
        }
        get("/cache") {
            call.respond(Result(data = animeService.getAllAnimeWithAdditionFromCache()))
        }
        get {
            call.respond(Result(data = animeService.getAllAnimeWithAdditionFromDB()))
        }
        get("/{animeId}") {
            val animeId =
                call.parameters["animeId"]!!.toULong()
            call.respond(Result(data = animeService.getAnimeById(animeId)))
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
            get("{id}") {
                val animeSeasonId =
                    call.parameters["id"]!!.toULong()
                call.respond(Result(data = animeService.getAnimeSeasonsWithAdditionAndAnimeById(animeSeasonId)))
            }
            get("/years") {
                call.respond(Result(data = animeService.getAnimeSeasonYears()))
            }
            get("/by-anime-id") {
                val animeId =
                    call.parameters["animeId"]!!.toULong()
                call.respond(Result(data = animeService.getAnimeSeasonsWithAdditionByAnimeId(animeId)))
            }
            get("/by-year-and-month-type") {
                val year = call.parameters["year"]!!.toInt()
                val monthType = call.parameters["monthType"]?.let {
                    AnimeMonthType.valueOf(it)
                }
                call.respond(
                    Result(
                        data = animeService.getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(
                            year,
                            monthType
                        )
                    )
                )
            }
            get("/section-map") {
                call.respond(Result(data = animeService.getAnimeSeasonSectionMap()))
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
                call.respond(Result(data = TmdbTool().searchAnimeTVForAnime(query)))
            }
            get("/search-anime-movie") {
                val query = call.parameters["query"]!!
                call.respond(Result(data = TmdbTool().searchAnimeMovieForAnimeMovie(query)))
            }
            get("/tv-detail") {
                val id = call.parameters["id"]!!.toInt()
                call.respond(Result(data = TmdbTool().getTvDetailsForAnime(id)))
            }
            get("/season-detail") {
                val showId = call.parameters["showId"]!!.toInt()
                val season = call.parameters["season"]!!.toInt()
                call.respond(Result(data = TmdbTool().getTvSeasonDetailsForAnimeSeason(showId, season)))
            }
            get("/movie-detail") {
                val id = call.parameters["id"]!!.toInt()
                call.respond(Result(data = TmdbTool().getMovieDetailsForAnimeMovie(id)))
            }
        }

        route("/file") {
            post("/season-file") {
                call.respond(
                    Result(
                        data = animeService.handleAnimeSeasonFile(call.receive())
                    )
                )
            }
        }
    }
}