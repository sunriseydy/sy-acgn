package dev.sunriseydy.acgn.client.anime.api

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeMovie
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.client.animeModuleApiEndPoint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-07-23 19:54
 */
class AnimeApi internal constructor(private val httpClient: HttpClient) {
    fun searchAnimeByName(name: String): Result<List<Anime>> = runBlocking {
        httpClient.get {
            animeApiEndPoint("name")
            parameter("name", name)
        }.body()
    }

    fun getAnimeById(animeId: ULong): Result<Anime> = runBlocking {
        httpClient.get {
            animeApiEndPoint(animeId.toString())
        }.body()
    }

    fun getAllAnimeFromCache(): Result<List<Anime>> = runBlocking {
        httpClient.get {
            animeApiEndPoint("cache")
        }.body()
    }

    fun getAllAnimeFromDb(): Result<List<Anime>> = runBlocking {
        httpClient.get {
            animeApiEndPoint()
        }.body()
    }

    fun refreshAnimeCache(): Result<Unit> = runBlocking {
        httpClient.get {
            animeApiEndPoint("refresh")
        }.body()
    }

    fun removeAnimeById(animeId: ULong): Result<Unit> = runBlocking {
        httpClient.delete {
            animeApiEndPoint(animeId.toString())
        }.body()
    }

    fun getAnimeSeasonsById(id: ULong): Result<AnimeSeason> = runBlocking {
        httpClient.get {
            animeSeasonApiEndPoint(id.toString())
        }.body()
    }

    fun getAnimeYears(): Result<List<Int>> = runBlocking {
        httpClient.get {
            animeSeasonApiEndPoint("years")
        }.body()
    }

    fun getAnimeSeasonsByAnimeId(animeId: ULong): Result<List<AnimeSeason>> = runBlocking {
        httpClient.get {
            animeSeasonApiEndPoint("by-anime-id")
            parameter("animeId", animeId)
        }.body()
    }

    fun getAnimeSeasonsByYearAndMonth(year: Int, monthType: AnimeMonthType): Result<List<AnimeSeason>> =
        runBlocking {
            httpClient.get {
                animeSeasonApiEndPoint("by-year-and-month-type")
                parameter("year", year)
                parameter("monthType", monthType)
            }.body()
        }

    fun getAnimeSeasonSectionMap(): Result<MutableMap<String, List<AnimeSeason>>> = runBlocking {
        httpClient.get {
            animeSeasonApiEndPoint("section-map")
        }.body()
    }

    fun saveAnimeSeason(animeSeason: AnimeSeason): Result<AnimeSeason> = runBlocking {
        httpClient.post {
            animeSeasonApiEndPoint()
            setBody(animeSeason)
        }.body()
    }

    fun removeAnimeSeasonById(seasonId: ULong): Result<Unit> = runBlocking {
        httpClient.delete {
            animeSeasonApiEndPoint(seasonId.toString())
        }.body()
    }

    fun searchTmdbAnimeTv(query: String): Result<List<Anime>> = runBlocking {
        httpClient.get {
            animeTmdbApiEndPoint("search-anime-tv")
            parameter("query", query)
        }.body()
    }

    fun searchTmdbAnimeMovie(query: String): Result<List<Anime>> = runBlocking {
        httpClient.get {
            animeTmdbApiEndPoint("search-anime-movie")
            parameter("query", query)
        }.body()
    }

    fun getTmdbAnimeTvDetail(id: ULong): Result<Anime> = runBlocking {
        httpClient.get {
            animeTmdbApiEndPoint("tv-detail")
            parameter("id", id)
        }.body()
    }

    fun getTmdbAnimeSeasonDetail(showId: Int, season: String): Result<AnimeSeason> = runBlocking {
        httpClient.get {
            animeTmdbApiEndPoint("season-detail")
            parameter("showId", showId)
            parameter("season", season)
        }.body()
    }

    fun getTmdbAnimeMovieDetail(id: Int): Result<AnimeMovie> = runBlocking {
        httpClient.get {
            animeTmdbApiEndPoint("movie-detail")
            parameter("id", id)
        }.body()
    }

    fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile): Result<Unit> = runBlocking {
        httpClient.post {
            animeFileApiEndPoint("season-file")
            setBody(animeSeasonFile)
        }.body()
    }

    private fun HttpRequestBuilder.animeApiEndPoint(vararg paths: String) {
        animeModuleApiEndPoint("anime", *paths)
    }

    private fun HttpRequestBuilder.animeSeasonApiEndPoint(vararg paths: String) {
        animeModuleApiEndPoint("anime", "season", *paths)
    }

    private fun HttpRequestBuilder.animeTmdbApiEndPoint(vararg paths: String) {
        animeModuleApiEndPoint("anime", "tmdb", *paths)
    }

    private fun HttpRequestBuilder.animeFileApiEndPoint(vararg paths: String) {
        animeModuleApiEndPoint("anime", "file", *paths)
    }
}