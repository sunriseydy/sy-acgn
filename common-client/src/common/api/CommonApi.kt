package dev.sunriseydy.acgn.client.common.api

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.base.interfaces.AdditionTypeInterface
import dev.sunriseydy.acgn.base.interfaces.AssociatedTypeInterface
import dev.sunriseydy.acgn.common.CommonModuleResource
import dev.sunriseydy.acgn.common.dto.*
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

private val logger = KotlinLogging.logger { }

/**
 * @author SunriseYDY
 * @date 2024-07-24 09:34
 */
class CommonApi internal constructor(private val httpClient: HttpClient) {

    suspend fun getAppInfo(language: Language? = null): Result<AppInfo> =
        httpClient.get(CommonModuleResource.Info(language = language)).body()
    

    suspend fun getLocalizations(): Result<MutableMap<String, String>> =
        httpClient.get(CommonModuleResource.Localization()).body()
    

    suspend fun getAllAppConfigFromDB(): Result<List<AppConfig>> =
        httpClient.get(CommonModuleResource.Config()).body()
    

    suspend fun getAppConfigs(): Result<MutableMap<String, Pair<AppConfig?, String?>>> =
        httpClient.get(CommonModuleResource.Config.Map()).body()
    

    suspend fun saveAppConfigs(configs: List<AppConfig>): Result<List<AppConfig>> =
        httpClient.post(CommonModuleResource.Config()) {
            setBody(configs)
        }.body()
    

    suspend fun getAdditions(
        associatedType: AssociatedTypeInterface,
        associatedId: ULong,
        additionalType: AdditionTypeInterface?
    ): Result<List<AdditionalInfo>> =
        httpClient.get(
            CommonModuleResource.Addition(
                associatedType = associatedType.key,
                associatedId = associatedId,
                additionalType = additionalType?.key
            )
        ).body()

    suspend fun saveAddition(addition: AdditionalInfo): Result<String> =
        httpClient.post(CommonModuleResource.Addition()) {
            setBody(addition)
        }.body()
    

    suspend fun deleteAddition(id: String): Result<Unit> =
        httpClient.delete(CommonModuleResource.Addition(id = id)).body()
    
    suspend fun getAttachFileBytes(id: String): ByteArray =
        httpClient.get(CommonModuleResource.AttachFile(id = id)) {
            header("X-Silent-Request", "true")
        }.body()

    suspend fun getProxyImageBytes(url: String): ByteArray =
        httpClient.get(CommonModuleResource.ProxyImage(url = url)) {
            header("X-Silent-Request", "true")
        }.body()

    /**
     * 发起 OpenAI 兼容流式对话并以 [Flow] 接收逐块输出
     *
     * 自动携带 `X-Silent-Request: true` 标头，跳过全局阻塞式全屏 Loading 遮罩。
     * 优先尝试从响应通道流式读取行；在 Wasm-JS / 浏览器等环境响应已被整体缓冲或通道无法流式读取时，
     * 自动回退为从完整响应文本（[HttpResponse.bodyAsText]）中逐行解析 SSE 数据块并发送。
     */
    fun chatStreamFlow(request: OpenAiChatRequest): Flow<OpenAiChatStreamChunk> = channelFlow {
        httpClient.preparePost(CommonModuleResource.OpenAi.Chat()) {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("X-Silent-Request", "true")
        }.execute { response ->
            var emittedAny = false
            try {
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readLine() ?: break
                    val chunk = parseSseLine(line)
                    if (chunk != null) {
                        emittedAny = true
                        send(chunk)
                    }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Failed reading OpenAI stream from channel, attempting fallback to bodyAsText" }
            }

            // 若通道流式读取未产生任何数据块（如 Wasm-JS 浏览器环境缓冲），回退至 bodyAsText
            if (!emittedAny) {
                try {
                    val fullText = response.bodyAsText()
                    fullText.lineSequence().forEach { line ->
                        val chunk = parseSseLine(line)
                        if (chunk != null) {
                            send(chunk)
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed reading OpenAI stream body as text" }
                }
            }
        }
    }

    private fun parseSseLine(line: String): OpenAiChatStreamChunk? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith(":")) {
            return null
        }
        if (trimmed.startsWith("data:")) {
            val data = trimmed.removePrefix("data:").trim()
            if (data == "[DONE]") {
                return null
            }
            return try {
                HttpClientFactory.jsonConfig.decodeFromString<OpenAiChatStreamChunk>(data)
            } catch (e: Exception) {
                logger.warn(e) { "Failed to decode OpenAI stream chunk JSON: $data" }
                null
            }
        }
        return null
    }
}