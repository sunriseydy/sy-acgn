package dev.sunriseydy.acgn.anime.tools

import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbMovieDetail
import app.moviebase.tmdb.model.TmdbShow
import app.moviebase.tmdb.model.TmdbShowDetail
import dev.sunriseydy.acgn.common.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-16 19:46
 */
class TmdbTool {
    private val language = LocalizationTool.currentLanguage.language
    private val ANIME_GENRE_ID: Int = 16

    private val tmdbClient = Tmdb3 {
        tmdbApiKey = AnimeModuleAppConfig.TmdbApiKey.configValue
    }

    suspend fun searchTV(query: String): List<TmdbShow> =
        tmdbClient.search.findShows(
            query = query,
            language = language,
            page = 1
        ).results

    suspend fun searchMovie(query: String): List<TmdbMovie> =
        tmdbClient.search.findMovies(
            query = query,
            page = 1,
            language = language
        ).results

    suspend fun searchAnimeTV(query: String): List<TmdbShow> =
        searchTV(query).filter { it.genresIds.contains(ANIME_GENRE_ID) }

    suspend fun searchAnimeMovie(query: String): List<TmdbMovie> =
        searchMovie(query).filter { it.genresIds.contains(ANIME_GENRE_ID) }

    suspend fun getTvDetails(id: Int): TmdbShowDetail {
        return tmdbClient.show.getDetails(id, language)
    }

    suspend fun getMovieDetails(id: Int): TmdbMovieDetail {
        return tmdbClient.movies.getDetails(id, language)
    }
}