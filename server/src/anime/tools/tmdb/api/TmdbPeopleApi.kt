package dev.sunriseydy.acgn.anime.tools.tmdb.api

import dev.sunriseydy.acgn.anime.tools.tmdb.core.endPointV3
import dev.sunriseydy.acgn.anime.tools.tmdb.core.parameterAppendResponses
import dev.sunriseydy.acgn.anime.tools.tmdb.core.parameterLanguage
import dev.sunriseydy.acgn.anime.tools.tmdb.model.AppendResponse
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbPersonDetail
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbPersonMovieCredits
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbPersonShowCredits
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get

class TmdbPeopleApi internal constructor(private val client: HttpClient) {

    suspend fun getDetails(
        personId: Int,
        language: String? = null,
        appendResponses: Iterable<AppendResponse>? = null
    ): TmdbPersonDetail = client.get {
        endPointPerson(personId)
        parameterLanguage(language)
        parameterAppendResponses(appendResponses)
    }.body()

    suspend fun getShowCredits(
        personId: Int,
        language: String? = null
    ): TmdbPersonShowCredits = client.get {
        endPointPerson(personId, "tv_credits")
        parameterLanguage(language)
    }.body()

    suspend fun getMovieCredits(
        personId: Int,
        language: String? = null
    ): TmdbPersonMovieCredits = client.get {
        endPointPerson(personId, "movie_credits")
        parameterLanguage(language)
    }.body()

    private fun HttpRequestBuilder.endPointPerson(personId: Int, vararg paths: String) {
        endPointV3("person", personId.toString(), *paths)
    }
}
