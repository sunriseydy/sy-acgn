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

/**
 * @author SunriseYDY
 * @date 2024-07-23 19:54
 */
class AnimeApi internal constructor(private val httpClient: HttpClient) {
    suspend fun searchAnimeByName(name: String): Result<List<Anime>> = httpClient.get {
        animeApiEndPoint("name")
        parameter("name", name)
    }.body()

    suspend fun getAnimeById(animeId: ULong): Result<Anime> = httpClient.get {
        animeApiEndPoint(animeId.toString())
    }.body()

    suspend fun getAllAnimeFromCache(): Result<List<Anime>> = httpClient.get {
        animeApiEndPoint("cache")
    }.body()

    suspend fun getAllAnimeFromDb(): Result<List<Anime>> = httpClient.get {
        animeApiEndPoint()
    }.body()

    suspend fun refreshAnimeCache(): Result<Unit> = httpClient.get {
        animeApiEndPoint("refresh")
    }.body()

    suspend fun removeAnimeById(animeId: ULong): Result<Unit> = httpClient.delete {
        animeApiEndPoint(animeId.toString())
    }.body()

    suspend fun getAnimeSeasonsById(id: ULong): Result<AnimeSeason> = httpClient.get {
        animeSeasonApiEndPoint(id.toString())
    }.body()

    suspend fun getAnimeYears(): Result<List<Int>> = httpClient.get {
        animeSeasonApiEndPoint("years")
    }.body()

    suspend fun getAnimeSeasonsByAnimeId(animeId: ULong): Result<List<AnimeSeason>> = httpClient.get {
        animeSeasonApiEndPoint("by-anime-id")
        parameter("animeId", animeId)
    }.body()

    suspend fun getAnimeSeasonsByYearAndMonth(year: Int, monthType: AnimeMonthType): Result<List<AnimeSeason>> =
        httpClient.get {
            animeSeasonApiEndPoint("by-year-and-month-type")
            parameter("year", year)
            parameter("monthType", monthType)
        }.body()

    suspend fun getAnimeSeasonSectionMap(): Result<MutableMap<String, List<AnimeSeason>>> = httpClient.get {
        animeSeasonApiEndPoint("section-map")
    }.body()

    suspend fun saveAnimeSeason(animeSeason: AnimeSeason): Result<AnimeSeason> = httpClient.post {
        animeSeasonApiEndPoint()
        setBody(animeSeason)
    }.body()

    suspend fun removeAnimeSeasonById(seasonId: ULong): Result<Unit> = httpClient.delete {
        animeSeasonApiEndPoint(seasonId.toString())
    }.body()

    suspend fun searchTmdbAnimeTv(query: String): Result<List<Anime>> = httpClient.get {
        animeTmdbApiEndPoint("search-anime-tv")
        parameter("query", query)
    }.body()

    suspend fun searchTmdbAnimeMovie(query: String): Result<List<Anime>> = httpClient.get {
        animeTmdbApiEndPoint("search-anime-movie")
        parameter("query", query)
    }.body()

    suspend fun getTmdbAnimeTvDetail(id: ULong): Result<Anime> = httpClient.get {
        animeTmdbApiEndPoint("tv-detail")
        parameter("id", id)
    }.body()

    suspend fun getTmdbAnimeSeasonDetail(showId: Int, season: String): Result<AnimeSeason> = httpClient.get {
        animeTmdbApiEndPoint("season-detail")
        parameter("showId", showId)
        parameter("season", season)
    }.body()

    suspend fun getTmdbAnimeMovieDetail(id: Int): Result<AnimeMovie> = httpClient.get {
        animeTmdbApiEndPoint("movie-detail")
        parameter("id", id)
    }.body()

    suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile): Result<Unit> = httpClient.post {
        animeFileApiEndPoint("season-file")
        setBody(animeSeasonFile)
    }.body()

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