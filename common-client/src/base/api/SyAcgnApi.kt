package dev.sunriseydy.acgn.client.base.api

import dev.sunriseydy.acgn.client.anime.api.AnimeApi
import dev.sunriseydy.acgn.client.anime.api.RssApi
import dev.sunriseydy.acgn.client.base.utils.getLocalServerConfig
import dev.sunriseydy.acgn.client.common.api.CommonApi
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * @author SunriseYDY
 * @date 2024-07-23 11:27
 */
class SyAcgnApi {
    private val httpClient by lazy {
        HttpClientFactory.buildHttpClient {
            install(Resources)
            defaultRequest {
                url {
                    takeFrom(getLocalServerConfig())
                }
                contentType(ContentType.Application.Json)
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