package dev.sunriseydy.acgn.client.anime.api

import dev.sunriseydy.acgn.anime.AnimeModuleResource
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeMovie
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.base.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*

/**
 * @author SunriseYDY
 * @date 2024-07-23 19:54
 */
class AnimeApi internal constructor(private val httpClient: HttpClient) {
    suspend fun searchAnimeByName(name: String): Result<List<Anime>> =
        httpClient.get(AnimeModuleResource.Anime.Name(name = name)).body()

    suspend fun getAnimeSeasonSectionMap(name: String?): Result<MutableMap<String, List<AnimeSeason>>> =
        httpClient.get(AnimeModuleResource.Anime.Season.SectionMap(name = name)).body()

    suspend fun getAnimeSeasonById(seasonId: ULong): Result<AnimeSeason> =
        httpClient.get(AnimeModuleResource.Anime.Season.Id(id = seasonId)).body()

    suspend fun getAnimeEpisodesBySeasonId(seasonId: ULong): Result<List<AnimeEpisode>> =
        httpClient.get(AnimeModuleResource.Anime.Season.Id.Episodes(parent = AnimeModuleResource.Anime.Season.Id(id = seasonId))).body()

    suspend fun syncAnimeSeasonEpisodes(seasonId: ULong): Result<List<AnimeEpisode>> =
        httpClient.post(AnimeModuleResource.Anime.Season.Id.SyncEpisodes(parent = AnimeModuleResource.Anime.Season.Id(id = seasonId))).body()

    suspend fun saveAnimeSeason(animeSeason: AnimeSeason): Result<AnimeSeason> =
        httpClient.post(AnimeModuleResource.Anime.Season()) {
            setBody(animeSeason)
        }.body()

    suspend fun removeAnimeSeasonById(seasonId: ULong): Result<Unit> =
        httpClient.delete(AnimeModuleResource.Anime.Season.Id(id = seasonId)).body()

    suspend fun removeAnimeEpisodeById(episodeId: ULong): Result<Unit> =
        httpClient.delete(AnimeModuleResource.Anime.Season.Episode.Id(episodeId = episodeId)).body()

    suspend fun searchTmdbAnimeTv(query: String): Result<List<Anime>> =
        httpClient.get(AnimeModuleResource.Anime.Tmdb.SearchTv(query = query)).body()

    suspend fun searchTmdbAnimeMovie(query: String): Result<List<Anime>> =
        httpClient.get(AnimeModuleResource.Anime.Tmdb.SearchMovie(query = query)).body()

    suspend fun getTmdbAnimeTvDetail(id: ULong): Result<Anime> =
        httpClient.get(AnimeModuleResource.Anime.Tmdb.TvDetail(id = id.toInt())).body()

    suspend fun getTmdbAnimeSeasonDetail(showId: Int, season: String): Result<AnimeSeason> =
        httpClient.get(AnimeModuleResource.Anime.Tmdb.SeasonDetail(showId = showId, season = season.toInt())).body()

    suspend fun getTmdbAnimeMovieDetail(id: Int): Result<AnimeMovie> =
        httpClient.get(AnimeModuleResource.Anime.Tmdb.MovieDetail(id = id)).body()

    suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile): Result<Unit> =
        httpClient.post(AnimeModuleResource.Anime.File.SeasonFile()) {
            setBody(animeSeasonFile)
        }.body()

    suspend fun searchBgmAnime(query: String): Result<List<AnimeSeason>> =
        httpClient.get(AnimeModuleResource.Anime.Bangumi.SearchAnime(query = query)).body()
    
}