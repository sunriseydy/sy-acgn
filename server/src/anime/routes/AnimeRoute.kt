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
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * 动漫模块 API 路由
 *
 * @author SunriseYDY
 * @date 2024-07-15 14:45
 */
fun Route.animeRoutes() {
    val animeService: AnimeService by application.dependencies
    val tmdbTool: TmdbTool by application.dependencies
    val bangumiTool: BangumiTool by application.dependencies

    // --- Anime ---
    get<AnimeModuleResource.Anime.Name> { resource ->
        call.respond(Result(data = animeService.searchAnimeByName(resource.name)))
    }

    // --- AnimeSeason ---
    get<AnimeModuleResource.Anime.Season.SectionMap> { resource ->
        call.respond(Result(data = animeService.getAnimeSeasonSectionMap(resource.name)))
    }
    get<AnimeModuleResource.Anime.Season.Id> { resource ->
        call.respond(Result(data = animeService.getAnimeSeasonById(resource.id)))
    }
    post<AnimeModuleResource.Anime.Season> {
        call.respond(Result(data = animeService.saveAnimeSeason(call.receive())))
    }
    delete<AnimeModuleResource.Anime.Season.Id> { resource ->
        call.respond(Result(data = animeService.removeAnimeSeasonById(resource.id)))
    }
    post<AnimeModuleResource.Anime.Season.Id.SyncEpisodes> { resource ->
        call.respond(Result(data = animeService.syncAnimeSeasonEpisodes(resource.parent.id)))
    }
    get<AnimeModuleResource.Anime.Season.Id.Episodes> { resource ->
        call.respond(Result(data = animeService.getAnimeEpisodesBySeasonId(resource.parent.id)))
    }

    // --- AnimeEpisode ---
    delete<AnimeModuleResource.Anime.Season.Episode.Id> { resource ->
        call.respond(Result(data = animeService.removeAnimeEpisodeById(resource.episodeId)))
    }

    // --- TMDB API ---
    get<AnimeModuleResource.Anime.Tmdb.SearchTv> { resource ->
        call.respond(Result(data = tmdbTool.searchAnimeTVForAnime(resource.query)))
    }
    get<AnimeModuleResource.Anime.Tmdb.SearchMovie> { resource ->
        call.respond(Result(data = tmdbTool.searchAnimeMovieForAnimeMovie(resource.query)))
    }
    get<AnimeModuleResource.Anime.Tmdb.TvDetail> { resource ->
        call.respond(Result(data = tmdbTool.getTvDetailsForAnime(resource.id)))
    }
    get<AnimeModuleResource.Anime.Tmdb.SeasonDetail> { resource ->
        call.respond(Result(data = tmdbTool.getTvSeasonDetailsForAnimeSeason(resource.showId, resource.season)))
    }
    get<AnimeModuleResource.Anime.Tmdb.MovieDetail> { resource ->
        call.respond(Result(data = tmdbTool.getMovieDetailsForAnimeMovie(resource.id)))
    }

    // --- Bangumi API ---
    get<AnimeModuleResource.Anime.Bangumi.SearchAnime> { resource ->
        call.respond(Result(data = bangumiTool.searchAnime(resource.query)))
    }
    get<AnimeModuleResource.Anime.Bangumi.SubjectDetail> { resource ->
        call.respond(Result(data = bangumiTool.getSubject(resource.id)))
    }

    // --- File ---
    post<AnimeModuleResource.Anime.File.SeasonFile> {
        call.respond(
            Result(
                data = animeService.handleAnimeSeasonFile(call.receive())
            )
        )
    }
}