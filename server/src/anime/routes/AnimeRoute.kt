package dev.sunriseydy.acgn.server.anime.routes

import dev.sunriseydy.acgn.anime.AnimeModuleResource
import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.server.anime.service.AnimeService
import dev.sunriseydy.acgn.server.anime.tools.BangumiTool
import dev.sunriseydy.acgn.server.anime.tools.TmdbTool
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * @author SunriseYDY
 * @date 2024-07-15 14:45
 */
fun Route.animeRoutes() {
    val animeService: AnimeService by application.dependencies
    get<AnimeModuleResource.Anime.Name> { resource ->
        call.respond(Result(data = animeService.searchAnimeByName(resource.name)))
    }
    get<AnimeModuleResource.Anime.Cache> {
        call.respond(Result(data = animeService.getAllAnimeWithAdditionFromCache()))
    }
    get<AnimeModuleResource.Anime> {
        call.respond(Result(data = animeService.getAllAnimeWithAdditionFromDB()))
    }
    get<AnimeModuleResource.Anime.Id> { resource ->
        call.respond(Result(data = animeService.getAnimeById(resource.animeId)))
    }
    put<AnimeModuleResource.Anime.Refresh> {
        call.respond(Result(data = animeService.refreshAnimeCache()))
    }
    delete<AnimeModuleResource.Anime.Id> { resource ->
        call.respond(Result(data = animeService.removeAnimeById(resource.animeId)))
    }

    get<AnimeModuleResource.Anime.Season.Id> { resource ->
        call.respond(Result(data = animeService.getAnimeSeasonsWithAdditionAndAnimeById(resource.id)))
    }
    get<AnimeModuleResource.Anime.Season.Years> {
        call.respond(Result(data = animeService.getAnimeSeasonYears()))
    }
    get<AnimeModuleResource.Anime.Season.ByAnimeId> { resource ->
        call.respond(Result(data = animeService.getAnimeSeasonsWithAdditionByAnimeId(resource.animeId)))
    }
    get<AnimeModuleResource.Anime.Season.ByYearAndMonth> { resource ->
        call.respond(
            Result(
                data = animeService.getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(
                    resource.year,
                    resource.monthType
                )
            )
        )
    }
    get<AnimeModuleResource.Anime.Season.SectionMap> {
        call.respond(Result(data = animeService.getAnimeSeasonSectionMap()))
    }
    post<AnimeModuleResource.Anime.Season> {
        call.respond(Result(data = animeService.saveAnimeSeason(call.receive())))
    }
    delete<AnimeModuleResource.Anime.Season.Id> { resource ->
        call.respond(Result(data = animeService.removeAnimeSeasonById(resource.id)))
    }

    delete<AnimeModuleResource.Anime.Season.Episode.Id> { resource ->
        call.respond(Result(data = animeService.removeAnimeEpisodeById(resource.episodeId)))
    }

    get<AnimeModuleResource.Anime.Tmdb.SearchTv> { resource ->
        call.respond(Result(data = TmdbTool().searchAnimeTVForAnime(resource.query)))
    }
    get<AnimeModuleResource.Anime.Tmdb.SearchMovie> { resource ->
        call.respond(Result(data = TmdbTool().searchAnimeMovieForAnimeMovie(resource.query)))
    }
    get<AnimeModuleResource.Anime.Tmdb.TvDetail> { resource ->
        call.respond(Result(data = TmdbTool().getTvDetailsForAnime(resource.id)))
    }
    get<AnimeModuleResource.Anime.Tmdb.SeasonDetail> { resource ->
        call.respond(Result(data = TmdbTool().getTvSeasonDetailsForAnimeSeason(resource.showId, resource.season)))
    }
    get<AnimeModuleResource.Anime.Tmdb.MovieDetail> { resource ->
        call.respond(Result(data = TmdbTool().getMovieDetailsForAnimeMovie(resource.id)))
    }

    get<AnimeModuleResource.Anime.Bangumi.SearchAnime> { resource ->
        call.respond(Result(data = application.dependencies.resolve<BangumiTool>().searchAnime(resource.query)))
    }
    get<AnimeModuleResource.Anime.Bangumi.SubjectDetail> { resource ->
        call.respond(Result(data = application.dependencies.resolve<BangumiTool>().getSubject(resource.id)))
    }

    post<AnimeModuleResource.Anime.File.SeasonFile> {
        call.respond(
            Result(
                data = animeService.handleAnimeSeasonFile(call.receive())
            )
        )
    }
}