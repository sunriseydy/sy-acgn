package dev.sunriseydy.acgn.server.anime.tools

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeMovie
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.common.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.server.anime.tools.tmdb.Tmdb3
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbMovie
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbMovieDetail
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbSeasonDetail
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbShow
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbShowDetail
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-16 19:46
 */
class TmdbTool {
    private val language = LocalizationTool.currentLanguage.let { "${it.languageCode}-${it.regionalCode}" }
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

    suspend fun searchAnimeTVForAnime(query: String) =
        searchAnimeTV(query).map { it.toAnime() }

    suspend fun searchAnimeMovie(query: String) =
        searchMovie(query).filter { it.genresIds.contains(ANIME_GENRE_ID) }

    suspend fun searchAnimeMovieForAnimeMovie(query: String) =
        searchAnimeMovie(query).map { it.toAnimeMovie() }

    suspend fun getTvDetails(id: Int) =
        tmdbClient.show.getDetails(id, language).copy(
            genres = emptyList(),
            lastEpisodeToAir = null,
            episodeRuntime = emptyList(),
            productionCompanies = null,
            networks = emptyList(),
            createdBy = null
        )

    suspend fun getTvDetailsForAnime(id: Int) =
        getTvDetails(id).toAnime()

    suspend fun getMovieDetails(id: Int) =
        tmdbClient.movies.getDetails(id, language)

    suspend fun getMovieDetailsForAnimeMovie(id: Int) =
        getMovieDetails(id).toAnimeMovie()

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

    suspend fun getTvSeasonDetailsForAnimeSeason(showId: Int, seasonNumber: Int) =
        getTvSeasonDetails(showId, seasonNumber).toAnimeSeason()

    private fun TmdbShow.toAnime() =
        Anime(
            id = ULong.MIN_VALUE,
            name = this.name,
            description = this.overview,
            firstAirDate = this.firstAirDate,
            tmdbId = this.id.toULong()
        )

    private fun TmdbMovie.toAnimeMovie() =
        AnimeMovie(
            id = ULong.MIN_VALUE,
            name = this.title,
            description = this.overview,
            releaseDate = this.releaseDate,
            tmdbId = this.id.toULong()
        )

    private fun TmdbShowDetail.toAnime() =
        Anime(
            id = ULong.MIN_VALUE,
            name = this.name,
            description = this.overview,
            firstAirDate = this.firstAirDate,
            lastAirDate = this.lastAirDate,
            numberOfSeasons = this.numberOfSeasons,
            numberOfEpisodes = this.numberOfEpisodes,
            tmdbId = this.id.toULong(),
            animeSeasons = this.seasons.map {
                AnimeSeason(
                    id = ULong.MIN_VALUE,
                    airDate = it.airDate,
                    numberOfEpisodes = it.numberOfEpisodes,
                    name = it.name,
                    season = it.seasonNumber,
                    description = it.overview,
                    animeId = ULong.MIN_VALUE,
                    year = it.airDate?.year ?: 0,
                    month = it.airDate?.monthNumber ?: 0,
                )
            }
        )

    private fun TmdbSeasonDetail.toAnimeSeason() =
        AnimeSeason(
            id = ULong.MIN_VALUE,
            airDate = this.airDate,
            numberOfEpisodes = this.numberOfEpisodes,
            name = this.name,
            season = this.seasonNumber,
            description = this.overview,
            animeId = ULong.MIN_VALUE,
            year = this.airDate?.year ?: 0,
            month = this.airDate?.monthNumber ?: 0,
        )

    private fun TmdbMovieDetail.toAnimeMovie() =
        AnimeMovie(
            id = ULong.MIN_VALUE,
            name = this.title,
            description = this.overview,
            releaseDate = this.releaseDate,
            tmdbId = this.id.toULong()
        )
}