package dev.sunriseydy.acgn.server.anime.tools.tmdb.api

import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.*
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.endPointV3
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterAppendResponses
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterIncludeImageLanguage
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterLanguage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class TmdbShowSeasonsApi(private val client: HttpClient) {

    suspend fun getDetails(
        showId: Int,
        seasonNumber: Int,
        language: String? = null,
        appendResponses: Iterable<AppendResponse>? = null,
        includeImageLanguages: String? = null
    ): TmdbSeasonDetail = client.get {
        endPointSeason(showId, seasonNumber)
        parameterLanguage(language)
        parameterAppendResponses(appendResponses)
        parameterIncludeImageLanguage(includeImageLanguages)
    }.body()

    suspend fun getVideos(showId: Int, seasonNumber: Int, language: String? = null): TmdbResult<TmdbVideo> =
        client.get {
            endPointSeason(showId, seasonNumber, "videos")
            parameterLanguage(language)
        }.body()

    suspend fun getTranslations(showId: Int, seasonNumber: Int): TmdbTranslations = client.get {
        endPointSeason(showId, seasonNumber, "translations")
    }.body()

    suspend fun getExternalIds(showId: Int, seasonNumber: Int): TmdbExternalIds = client.get {
        endPointSeason(showId, seasonNumber, "external_ids")
    }.body()

    private fun HttpRequestBuilder.endPointSeason(showId: Int, seasonNumber: Int, vararg paths: String) {
        endPointV3("tv", showId.toString(), "season", seasonNumber.toString(), *paths)
    }
}
