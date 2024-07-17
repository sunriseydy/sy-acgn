package dev.sunriseydy.acgn.anime.tools.tmdb.api

import dev.sunriseydy.acgn.anime.tools.tmdb.core.endPointV3
import dev.sunriseydy.acgn.anime.tools.tmdb.core.getByPaths
import dev.sunriseydy.acgn.anime.tools.tmdb.core.json
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbAccountDetails
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbFavoriteRequestBody
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbMediaType
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbMoviePageResult
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbStatusResult
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbWatchlistRequestBody
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class TmdbAccountApi internal constructor(private val client: HttpClient) {

    /**
     * Get your account details.
     * @see [Documentation](https://developers.themoviedb.org/3/account)
     */
    suspend fun getDetails(): TmdbAccountDetails = client.getByPaths("account")

    suspend fun getFavorites(accountId: Int, mediaType: TmdbMediaType): TmdbMoviePageResult = when (mediaType) {
        TmdbMediaType.MOVIE -> getFavoriteMovies(accountId)
        TmdbMediaType.SHOW -> getFavoriteShows(accountId)
        else -> throw IllegalArgumentException("Only movies and shows are supported.")
    }

    suspend fun getFavoriteMovies(accountId: Int): TmdbMoviePageResult = client.getByPaths(*pathAccount(accountId, "favorite", "movies"))

    suspend fun getFavoriteShows(accountId: Int): TmdbMoviePageResult = client.getByPaths(*pathAccount(accountId, "favorite", "tv"))

    /**
     * POST /account/{account_id}/favorite
     *
     * This method allows you to mark a movie or TV show as a favorite item.
     * @see [Documentation](https://developers.themoviedb.org/3/account/mark-as-favorite)
     */
    suspend fun markFavorite(accountId: Int, requestBody: TmdbFavoriteRequestBody): TmdbStatusResult = client.post {
        endPointAccount(accountId, "favorite")
        json()
        setBody(requestBody)
    }.body()

    suspend fun markFavorite(
        accountId: Int,
        mediaType: TmdbMediaType,
        mediaId: Int,
        favorite: Boolean
    ): TmdbStatusResult = markFavorite(accountId, TmdbFavoriteRequestBody(mediaType, mediaId, favorite))

    suspend fun addWatchlist(accountId: Int, requestBody: TmdbWatchlistRequestBody): TmdbStatusResult = client.post {
        endPointAccount(accountId, "watchlist")
        json()
        setBody(requestBody)
    }.body()

    private fun HttpRequestBuilder.endPointAccount(accountId: Int, vararg paths: String) {
        endPointV3("account", accountId.toString(), *paths)
    }

    private fun pathAccount(accountId: Int, vararg paths: String) = arrayOf("account", accountId.toString(), *paths)
}
