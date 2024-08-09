package dev.sunriseydy.acgn.server.anime.tools.tmdb.api

import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.*
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.endPointV3
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterAppendResponses
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterIncludeImageLanguage
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterLanguage
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterPage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class TmdbShowApi internal constructor(private val client: HttpClient) {

    suspend fun getDetails(
        showId: Int,
        language: String? = null,
        appendResponses: Iterable<AppendResponse>? = null
    ): TmdbShowDetail = client.get {
        endPointShow(showId)
        parameterLanguage(language)
        parameterAppendResponses(appendResponses)
    }.body()

    suspend fun getTranslations(showId: Int): TmdbTranslations = client.get {
        endPointShow(showId, "translations")
    }.body()

    /**
     * Get the primary TV show details by id.
     * @see [Documentation] (https://developers.themoviedb.org/3/tv/get-tv-details)
     *
     * @param includeImageLanguage If you want to include a fallback language, should be a comma seperated value like 'en,null'
     */
    suspend fun getImages(
        showId: Int,
        language: String? = null,
        includeImageLanguage: String? = null
    ): TmdbImages = client.get {
        endPointShow(showId, "images")
        parameterLanguage(language)
        parameterIncludeImageLanguage(includeImageLanguage)
    }.body()

    suspend fun getAggregateCredits(
        showId: Int,
        language: String? = null
    ): TmdbAggregateCredits = client.get {
        endPointShow(showId, "aggregate_credits")
        parameterLanguage(language)
    }.body()

    suspend fun getRecommendations(
        showId: Int,
        page: Int,
        language: String? = null
    ): TmdbShowPageResult = client.get {
        endPointShow(showId, "recommendations")
        parameterPage(page)
        parameterLanguage(language)
    }.body()

    suspend fun getWatchProviders(showId: Int): TmdbWatchProviderResult = client.get {
        endPointShow(showId, "watch", "providers")
    }.body()

    suspend fun popular(
        page: Int,
        language: String? = null,
    ): TmdbShowPageResult = client.get {
        endPointV3("tv", "popular")
        parameterPage(page)
        parameterLanguage(language)
    }.body()

    private fun HttpRequestBuilder.endPointShow(showId: Int, vararg paths: String) {
        endPointV3("tv", showId.toString(), *paths)
    }
}
