package dev.sunriseydy.acgn.client.common.api

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.base.interfaces.AdditionTypeInterface
import dev.sunriseydy.acgn.base.interfaces.AssociatedTypeInterface
import dev.sunriseydy.acgn.common.CommonModuleResource
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.dto.AppInfo
import dev.sunriseydy.acgn.common.dto.OpenAiChatRequest
import dev.sunriseydy.acgn.common.dto.OpenAiChatStreamChunk
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

    /**
     * 发起 OpenAI 兼容流式对话并以 [Flow] 接收逐块输出
     *
     * 自动携带 `X-Silent-Request: true` 标头，跳过全局阻塞式全屏 Loading 遮罩。
     */
    fun chatStreamFlow(request: OpenAiChatRequest): Flow<OpenAiChatStreamChunk> = flow {
        httpClient.preparePost(CommonModuleResource.OpenAi.Chat()) {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("X-Silent-Request", "true")
        }.execute { response ->
            val channel = response.bodyAsChannel()
            @Suppress("DEPRECATION")
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith(":")) {
                    continue
                }
                if (trimmed.startsWith("data:")) {
                    val data = trimmed.removePrefix("data:").trim()
                    if (data == "[DONE]") {
                        break
                    }
                    try {
                        val chunk = HttpClientFactory.jsonConfig.decodeFromString<OpenAiChatStreamChunk>(data)
                        emit(chunk)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}