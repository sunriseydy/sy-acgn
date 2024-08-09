package dev.sunriseydy.acgn.server.anime.tools.tmdb

import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.Tmdb4AccountApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.Tmdb4AuthenticationApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.Tmdb4ListApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.HttpClientFactory
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.TmdbDsl
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.interceptRequest
import io.ktor.client.*
import io.ktor.client.request.*

@TmdbDsl
fun Tmdb4(block: TmdbClientConfig.() -> Unit): Tmdb4 {
    val config = TmdbClientConfig().apply(block)
    return Tmdb4(config)
}

class Tmdb4 internal constructor(
    private val config: TmdbClientConfig
) {

    constructor(tmdbApiKey: String) : this(TmdbClientConfig.withKey(tmdbApiKey))

    init {
        check(!config.tmdbApiKey.isNullOrBlank()) {
            "TMDB API key is unavailable. Set the tmdbApiKey when instantiate the TMDB client."
        }
    }

    private val client by lazy {
        HttpClientFactory.buildHttpClient(
            version = TmdbVersion.V4,
            config = config,
            useAuthentication = true
        ).apply {
            interceptRequest {
                it.parameter(TmdbUrlParameter.API_KEY, config.tmdbApiKey)
            }
        }
    }

    private val clientForAuth by lazy {
        HttpClientFactory.buildHttpClient(TmdbVersion.V4, config).apply {
            interceptRequest {
                it.parameter(TmdbUrlParameter.API_KEY, config.tmdbApiKey)
                config.tmdbAuthCredentials?.authenticationToken?.let { token ->
                    it.bearerAuth(token)
                }
            }
        }
    }

    val account by buildApi(::Tmdb4AccountApi)
    val auth by lazy { Tmdb4AuthenticationApi(clientForAuth) }
    val list by buildApi(::Tmdb4ListApi)

    private inline fun <T> buildApi(crossinline builder: (HttpClient) -> T) = lazy { builder(client) }
}
