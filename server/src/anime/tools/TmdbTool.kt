package dev.sunriseydy.acgn.anime.tools

import dev.sunriseydy.acgn.anime.tools.tmdb.Tmdb3
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbMovieDetail
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

    suspend fun searchTV(query: String) =
        tmdbClient.search.findShows(
            query = query,
            language = language,
            page = 1
        ).results

    suspend fun searchMovie(query: String) =
        tmdbClient.search.findMovies(
            query = query,
            page = 1,
            language = language
        ).results

    suspend fun searchAnimeTV(query: String) =
        searchTV(query).filter { it.genresIds.contains(ANIME_GENRE_ID) }

    suspend fun searchAnimeMovie(query: String) =
        searchMovie(query).filter { it.genresIds.contains(ANIME_GENRE_ID) }

    suspend fun getTvDetails(id: Int) =
        tmdbClient.show.getDetails(id, language).copy(
            genres = emptyList(),
            lastEpisodeToAir = null,
            episodeRuntime = emptyList(),
            productionCompanies = null,
            networks = emptyList(),
            createdBy = null
        )

    suspend fun getMovieDetails(id: Int): TmdbMovieDetail =
        tmdbClient.movies.getDetails(id, language)

    suspend fun getTvSeasonDetails(showId: Int, seasonNumber: Int) =
        tmdbClient.showSeasons.getDetails(showId, seasonNumber, language)
            .let {
                var episodes = it.episodes
                episodes = episodes?.map {
                    it.copy(
                        crew = null,
                        guestStars = null
                    )
                }
                it.copy(episodes = episodes)
            }
}