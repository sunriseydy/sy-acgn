package dev.sunriseydy.acgn.client.anime.api

import dev.sunriseydy.acgn.anime.AnimeModuleResource
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeMovie
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.base.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-07-23 19:54
 */
class AnimeApi internal constructor(private val httpClient: HttpClient) {
    fun searchAnimeByName(name: String): Result<List<Anime>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Name(name = name)).body()
    }

    fun getAnimeById(animeId: ULong): Result<Anime> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Id(animeId = animeId)).body()
    }

    fun getAllAnimeFromCache(): Result<List<Anime>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Cache()).body()
    }

    fun getAllAnimeFromDb(): Result<List<Anime>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime()).body()
    }

    fun refreshAnimeCache(): Result<Unit> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Refresh()).body()
    }

    fun removeAnimeById(animeId: ULong): Result<Unit> = runBlocking {
        httpClient.delete(AnimeModuleResource.Anime.Id(animeId = animeId)).body()
    }

    fun getAnimeSeasonsById(id: ULong): Result<AnimeSeason> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Season.Id(id = id)).body()
    }

    fun getAnimeYears(): Result<List<Int>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Season.Years()).body()
    }

    fun getAnimeSeasonsByAnimeId(animeId: ULong): Result<List<AnimeSeason>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Season.ByAnimeId(animeId = animeId)).body()
    }

    fun getAnimeSeasonsByYearAndMonth(year: Int, monthType: AnimeMonthType): Result<List<AnimeSeason>> =
        runBlocking {
            httpClient.get(AnimeModuleResource.Anime.Season.ByYearAndMonth(year = year, monthType = monthType)).body()
        }

    fun getAnimeSeasonSectionMap(): Result<MutableMap<String, List<AnimeSeason>>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Season.SectionMap()).body()
    }

    fun saveAnimeSeason(animeSeason: AnimeSeason): Result<AnimeSeason> = runBlocking {
        httpClient.post(AnimeModuleResource.Anime.Season()) {
            setBody(animeSeason)
        }.body()
    }

    fun removeAnimeSeasonById(seasonId: ULong): Result<Unit> = runBlocking {
        httpClient.delete(AnimeModuleResource.Anime.Season.Id(id = seasonId)).body()
    }

    fun searchTmdbAnimeTv(query: String): Result<List<Anime>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Tmdb.SearchTv(query = query)).body()
    }

    fun searchTmdbAnimeMovie(query: String): Result<List<Anime>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Tmdb.SearchMovie(query = query)).body()
    }

    fun getTmdbAnimeTvDetail(id: ULong): Result<Anime> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Tmdb.TvDetail(id = id.toInt())).body()
    }

    fun getTmdbAnimeSeasonDetail(showId: Int, season: String): Result<AnimeSeason> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Tmdb.SeasonDetail(showId = showId, season = season.toInt())).body()
    }

    fun getTmdbAnimeMovieDetail(id: Int): Result<AnimeMovie> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Tmdb.MovieDetail(id = id)).body()
    }

    fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Anime.File.SeasonFile()) {
            setBody(animeSeasonFile)
        }.body()
    }

    fun searchBgmAnime(query: String): Result<List<AnimeSeason>> = runBlocking {
        httpClient.get(AnimeModuleResource.Anime.Bangumi.SearchAnime(query = query)).body()
    }
}