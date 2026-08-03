package dev.sunriseydy.acgn.server.anime.tools

import dev.sunriseydy.acgn.anime.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeMovie
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.server.anime.tools.tmdb.Tmdb3
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.*
import dev.sunriseydy.acgn.tools.LocalizationTool
import kotlinx.datetime.number
import kotlinx.serialization.json.Json

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
                episodes = episodes?.map { it ->
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
            tmdbId = this.id.toULong(),
            additions = listOf(
                AdditionalInfo(
                    "",
                    ULong.MIN_VALUE,
                    AnimeAssociatedType.ANIME.key,
                    AnimeAdditionType.TmdbJson.key,
                    Json.encodeToString(this),
                )
            ),
        )

    private fun TmdbMovie.toAnimeMovie() =
        AnimeMovie(
            id = ULong.MIN_VALUE,
            name = this.title,
            description = this.overview,
            releaseDate = this.releaseDate,
            tmdbId = this.id.toULong(),
            additions = listOf(
                AdditionalInfo(
                    "",
                    ULong.MIN_VALUE,
                    AnimeAssociatedType.ANIME_MOVIE.key,
                    AnimeAdditionType.TmdbJson.key,
                    Json.encodeToString(this),
                )
            ),
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
                    animeId = ULong.MIN_VALUE,
                    name = it.name,
                    description = it.overview,
                    season = it.seasonNumber,
                    numberOfEpisodes = it.numberOfEpisodes,
                    year = it.airDate?.year ?: 0,
                    month = it.airDate?.month?.number ?: 0,
                    airDate = it.airDate,
                    tmdbId = it.id.toULong(),
                    additions = listOf(
                        AdditionalInfo(
                            "",
                            ULong.MIN_VALUE,
                            AnimeAssociatedType.ANIME_SEASON.key,
                            AnimeAdditionType.TmdbJson.key,
                            Json.encodeToString(it),
                        )
                    ),
                )
            },
            additions = listOf(
                AdditionalInfo(
                    "",
                    ULong.MIN_VALUE,
                    AnimeAssociatedType.ANIME.key,
                    AnimeAdditionType.TmdbJson.key,
                    Json.encodeToString(this),
                )
            ),
        )

    private fun TmdbSeasonDetail.toAnimeSeason() =
        AnimeSeason(
            id = ULong.MIN_VALUE,
            animeId = ULong.MIN_VALUE,
            name = this.name,
            description = this.overview,
            season = this.seasonNumber,
            numberOfEpisodes = this.numberOfEpisodes,
            year = this.airDate?.year ?: 0,
            month = this.airDate?.month?.number ?: 0,
            airDate = this.airDate,
            tmdbId = this.id.toULong(),
            animeEpisodes = this.episodes.orEmpty().map { it.toAnimeEpisode() },
            additions = listOf(
                AdditionalInfo(
                    "",
                    ULong.MIN_VALUE,
                    AnimeAssociatedType.ANIME_SEASON.key,
                    AnimeAdditionType.TmdbJson.key,
                    Json.encodeToString(this),
                )
            ),
        )

    private fun TmdbEpisode.toAnimeEpisode() =
        AnimeEpisode(
            id = ULong.MIN_VALUE,
            animeId = ULong.MIN_VALUE,
            animeSeasonId = ULong.MIN_VALUE,
            name = this.name?.takeIf { it.isNotBlank() } ?: "第${this.episodeNumber}集",
            description = this.overview,
            episode = this.episodeNumber,
            airDate = this.airDate,
            tmdbId = this.id.toULong(),
            additions = listOf(
                AdditionalInfo(
                    "",
                    ULong.MIN_VALUE,
                    AnimeAssociatedType.ANIME_EPISODE.key,
                    AnimeAdditionType.TmdbJson.key,
                    Json.encodeToString(this),
                )
            ),
        )

    private fun TmdbMovieDetail.toAnimeMovie() =
        AnimeMovie(
            id = ULong.MIN_VALUE,
            name = this.title,
            description = this.overview,
            releaseDate = this.releaseDate,
            tmdbId = this.id.toULong(),
            additions = listOf(
                AdditionalInfo(
                    "",
                    ULong.MIN_VALUE,
                    AnimeAssociatedType.ANIME_MOVIE.key,
                    AnimeAdditionType.TmdbJson.key,
                    Json.encodeToString(this),
                )
            ),
        )
}
