package dev.sunriseydy.acgn.client.base.api

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.api.AnimeApi
import dev.sunriseydy.acgn.client.anime.api.RssApi
import dev.sunriseydy.acgn.client.base.components.showError
import dev.sunriseydy.acgn.client.base.utils.getLocalServerConfig
import dev.sunriseydy.acgn.client.common.api.CommonApi
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
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

fun <T> Result<T>.onSuccess(
    appState: AppState? = null,
    onSuccess: () -> Unit = { },
    onError: (String) -> Unit = { }
) {
    try {
        this.checkSuccess()
        onSuccess()
    } catch (e: Exception) {
        val message = e.message ?: "API Error"
        appState?.showError(message)
        onError(message)
    }
}

fun <T> Result<T>.onSuccessData(
    appState: AppState? = null,
    onSuccess: (T) -> Unit = { },
    onError: (String) -> Unit = { throw error(it) }
) {
    try {
        onSuccess(this.checkSuccessAndNotNull())
    } catch (e: Exception) {
        val message = "API Error: ${e.message ?: ""}"
        appState?.showError(message)
        onError(message)
    }
}