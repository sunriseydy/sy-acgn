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

/**
 * @author SunriseYDY
 * @date 2024-07-23 19:54
 */
class AnimeApi internal constructor(private val httpClient: HttpClient) {
    suspend fun searchAnimeByName(name: String): Result<List<Anime>> =
        httpClient.get(AnimeModuleResource.Anime.Name(name = name)).body()
    

    suspend fun getAnimeById(animeId: ULong): Result<Anime> =
        httpClient.get(AnimeModuleResource.Anime.Id(animeId = animeId)).body()
    

    suspend fun getAllAnimeFromCache(): Result<List<Anime>> =
        httpClient.get(AnimeModuleResource.Anime.Cache()).body()
    

    suspend fun getAllAnimeFromDb(): Result<List<Anime>> =
        httpClient.get(AnimeModuleResource.Anime()).body()
    

    suspend fun refreshAnimeCache(): Result<Unit> =
        httpClient.get(AnimeModuleResource.Anime.Refresh()).body()
    

    suspend fun removeAnimeById(animeId: ULong): Result<Unit> =
        httpClient.delete(AnimeModuleResource.Anime.Id(animeId = animeId)).body()
    

    suspend fun getAnimeSeasonsById(id: ULong): Result<AnimeSeason> =
        httpClient.get(AnimeModuleResource.Anime.Season.Id(id = id)).body()
    

    suspend fun getAnimeYears(): Result<List<Int>> =
        httpClient.get(AnimeModuleResource.Anime.Season.Years()).body()
    

    suspend fun getAnimeSeasonsByAnimeId(animeId: ULong): Result<List<AnimeSeason>> =
        httpClient.get(AnimeModuleResource.Anime.Season.ByAnimeId(animeId = animeId)).body()
    

    suspend fun getAnimeSeasonsByYearAndMonth(year: Int, monthType: AnimeMonthType): Result<List<AnimeSeason>> =
            httpClient.get(AnimeModuleResource.Anime.Season.ByYearAndMonth(year = year, monthType = monthType)).body()
        

    suspend fun getAnimeSeasonSectionMap(): Result<MutableMap<String, List<AnimeSeason>>> =
        httpClient.get(AnimeModuleResource.Anime.Season.SectionMap()).body()
    
    suspend fun searchAnimeSeasonSectionMapByName(name: String): Result<MutableMap<String, List<AnimeSeason>>> =
        httpClient.get(AnimeModuleResource.Anime.Season.SearchByName(name = name)).body()
    

    suspend fun saveAnimeSeason(animeSeason: AnimeSeason): Result<AnimeSeason> =
        httpClient.post(AnimeModuleResource.Anime.Season()) {
            setBody(animeSeason)
        }.body()
    

    suspend fun removeAnimeSeasonById(seasonId: ULong): Result<Unit> =
        httpClient.delete(AnimeModuleResource.Anime.Season.Id(id = seasonId)).body()
    

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