package dev.sunriseydy.acgn.client.base.api

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.api.AnimeApi
import dev.sunriseydy.acgn.client.anime.api.RssApi
import dev.sunriseydy.acgn.client.base.components.showError
import dev.sunriseydy.acgn.client.base.utils.getLocalServerConfig
import dev.sunriseydy.acgn.client.common.api.CommonApi
import dev.sunriseydy.acgn.client.game.api.GameApi
import dev.sunriseydy.acgn.client.novel.api.NovelApi
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * SY-ACGN 主 API 客户端
 *
 * 负责构建和管理 HTTP 客户端以及各个模块（Rss, Anime, Common）的 API 实例。
 *
 * @author SunriseYDY
 * @date 2024-07-23 11:27
 */
@OptIn(ExperimentalAtomicApi::class)
class SyAcgnApi {
    private val activeRequestCount = AtomicInt(0)
    private val _isLoading = mutableStateOf(false)

    /**
     * 页面全局 Loading 状态
     */
    val isLoading: State<Boolean> get() = _isLoading

    private fun onRequestStart() {
        if (activeRequestCount.fetchAndAdd(1) == 0) {
            _isLoading.value = true
        }
    }

    private fun onRequestEnd() {
        if (activeRequestCount.addAndFetch(-1) <= 0) {
            activeRequestCount.store(0)
            _isLoading.value = false
        }
    }

    /**
     * 惰性初始化的 HTTP 客户端
     *
     * 配置了资源插件、默认请求 URL（从本地配置获取）和 JSON 内容类型。
     */
    private val httpClient by lazy {
        HttpClientFactory.buildHttpClient(logLevel = LogLevel.BODY) {
            install(Resources)
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            defaultRequest {
                url {
                    takeFrom(getLocalServerConfig())
                }
                contentType(ContentType.Application.Json)
            }
        }.apply {
            plugin(HttpSend).intercept { request ->
                val isSilent = request.headers["X-Silent-Request"]?.toBoolean() ?: false
                if (!isSilent) {
                    onRequestStart()
                }
                try {
                    execute(request)
                } finally {
                    if (!isSilent) {
                        onRequestEnd()
                    }
                }
            }
        }
    }

    // 各个模块的 API 实例，惰性加载
    val rss by buildApi(::RssApi)
    val anime by buildApi(::AnimeApi)
    val common by buildApi(::CommonApi)
    val novel by buildApi(::NovelApi)
    val game by buildApi(::GameApi)

    /**
     * 构建 API 实例的辅助函数
     *
     * @param builder 接受 HttpClient 并返回 API 实例的函数
     */
    private inline fun <T> buildApi(crossinline builder: (HttpClient) -> T) = lazy {
        builder(httpClient)
    }
}

/**
 * Result<T> 的扩展函数，用于处理成功的响应（无返回值）。
 *
 * 检查 Result 是否成功，如果成功则执行 onSuccess 回调。
 * 如果失败或发生异常，显示错误信息并执行 onError 回调。
 *
 * @param appState 应用状态，用于显示错误信息（可选）
 * @param onSuccess 成功时的回调
 * @param onError 失败时的回调
 */
fun <T> Result<T>.onSuccess(
    appState: AppState? = null,
    onSuccess: () -> Unit = { },
    onError: (String) -> Unit = { }
) {
    try {
        this.checkSuccess()
        onSuccess()
    } catch (e: Exception) {
        val message = "API Error: ${e.message ?: "Unknown error"}"
        appState?.showError(message)
        onError(message)
    }
}

/**
 * Result<T> 的扩展函数，用于处理包含数据的成功响应。
 *
 * 检查 Result 是否成功且数据不为空，如果满足则执行 onSuccess 回调处理数据。
 * 如果失败或发生异常，显示错误信息并执行 onError 回调。
 *
 * @param appState 应用状态，用于显示错误信息（可选）
 * @param onSuccess 成功时的回调，接收数据 T
 * @param onError 失败时的回调
 */
fun <T> Result<T>.onSuccessData(
    appState: AppState? = null,
    onSuccess: (T) -> Unit = { },
    onError: (String) -> Unit = { }
) {
    try {
        onSuccess(this.checkSuccessAndNotNull())
    } catch (e: Exception) {
        val message = "API Error: ${e.message ?: "Unknown error"}"
        appState?.showError(message)
        onError(message)
    }
}