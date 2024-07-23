package dev.sunriseydy.acgn

import dev.sunriseydy.acgn.anime.api.AnimeApi
import dev.sunriseydy.acgn.anime.api.RssApi
import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.appendPathSegments

/**
 * @author SunriseYDY
 * @date 2024-07-23 11:27
 */
class SyAcgnApi {
    private val httpClient by lazy {
        HttpClientFactory.buildHttpClient {
            defaultRequest {
                url(CommonModuleAppConfig.AppServer.configValue)
            }
        }
    }

    val rss by buildApi(::RssApi)
    val anime by buildApi(::AnimeApi)

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