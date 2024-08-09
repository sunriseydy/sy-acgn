package dev.sunriseydy.acgn.server.anime.tools.tmdb.core

import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbErrorResponse

@Suppress("MemberVisibilityCanBePrivate")
class TmdbException(
    val tmdbResponse: TmdbErrorResponse,
    caused: Throwable? = null
) : IllegalStateException(
    "Status code: ${tmdbResponse.statusCode}. Message: \"${tmdbResponse.statusMessage}\"",
    caused
) {

    val statusCode get() = tmdbResponse.statusCode
}
