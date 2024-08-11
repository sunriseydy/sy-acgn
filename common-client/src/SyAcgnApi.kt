package dev.sunriseydy.acgn.client

import dev.sunriseydy.acgn.client.anime.api.AnimeApi
import dev.sunriseydy.acgn.client.anime.api.RssApi
import dev.sunriseydy.acgn.client.common.api.CommonApi
import dev.sunriseydy.acgn.client.utils.getLocalServerConfig
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom

/**
 * @author SunriseYDY
 * @date 2024-07-23 11:27
 */
class SyAcgnApi {
    private val httpClient by lazy {
        HttpClientFactory.buildHttpClient {
            defaultRequest {
                url {
                    takeFrom(getLocalServerConfig())
                }
            }
        }
    }

    val rss by buildApi(::RssApi)
    val anime by buildApi(::AnimeApi)
    val common by buildApi(::CommonApi)

    private inline fun <T> buildApi(crossinline builder: (HttpClient) -> T) = lazy {
        builder(httpClient)
    }
}

internal fun HttpRequestBuilder.apiEndPoint(vararg paths: String) {
    url {
        appendPathSegments("api", *paths)
    }
}

internal fun HttpRequestBuilder.animeModuleApiEndPoint(vararg paths: String) {
    apiEndPoint("anime", *paths)
}

internal fun HttpRequestBuilder.commonModuleApiEndPoint(vararg paths: String) {
    apiEndPoint("common", *paths)
}