package dev.sunriseydy.acgn.server.common.tools

import dev.sunriseydy.acgn.base.exception.MessageException
import dev.sunriseydy.acgn.common.dto.OpenAiChatRequest
import dev.sunriseydy.acgn.common.dto.OpenAiChatStreamChunk
import dev.sunriseydy.acgn.common.enums.CommonModuleError
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private val logger = KotlinLogging.logger { }

/**
 * OpenAI 兼容 API 外部调用工具类
 *
 * 封装与兼容 OpenAI Chat Completions 协议的 HTTP 客户端交互，支持流式 SSE 响应解析。
 *
 * @author SunriseYDY
 * @date 2026-10-17
 */
class OpenAiTool(
    private val client: HttpClient = HttpClientFactory.buildHttpClient()
) {

    /**
     * 规整化 Chat Completions 请求 URL
     *
     * 兼容用户输入的各种常见 endpoint 格式：
     * - `https://api.openai.com/v1` -> `https://api.openai.com/v1/chat/completions`
     * - `https://api.openai.com/v1/` -> `https://api.openai.com/v1/chat/completions`
     * - `https://api.openai.com/v1/chat/completions` -> `https://api.openai.com/v1/chat/completions`
     */
    fun resolveChatUrl(endpoint: String): String {
        val trimmed = endpoint.trim().trimEnd('/')
        return if (trimmed.endsWith("/chat/completions")) {
            trimmed
        } else {
            "$trimmed/chat/completions"
        }
    }

    /**
     * 发起流式对话请求并以 [Flow] 逐块输出 [OpenAiChatStreamChunk]
     *
     * @param request 对话请求体
     * @param endpoint OpenAI 兼容端点 URL
     * @param apiKey API 访问密钥
     * @param model 调用的模型名称
     * @return 产生 [OpenAiChatStreamChunk] 的冷数据流
     */
    suspend fun chatStream(
        request: OpenAiChatRequest,
        endpoint: String,
        apiKey: String,
        model: String,
    ): Flow<OpenAiChatStreamChunk> = flow {
        val url = resolveChatUrl(endpoint)
        val finalRequest = request.copy(model = model, stream = true)

        logger.debug { "Requesting OpenAI streaming completion to: $url with model: $model" }

        client.preparePost(url) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(finalRequest)
        }.execute { response ->
            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                logger.error { "OpenAI API request failed: status=${response.status}, body=$errorBody" }
                throw MessageException(CommonModuleError.OPENAI_REQUEST_FAILED)
            }

            val channel: ByteReadChannel = response.bodyAsChannel()
            @Suppress("DEPRECATION")
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                val trimmed = line.trim()

                // 忽略空行以及 SSE 心跳/注释（以冒号开头）
                if (trimmed.isEmpty() || trimmed.startsWith(":")) {
                    continue
                }

                if (trimmed.startsWith("data:")) {
                    val data = trimmed.removePrefix("data:").trim()
                    if (data == "[DONE]") {
                        logger.debug { "OpenAI stream received [DONE]" }
                        break
                    }

                    try {
                        val chunk = HttpClientFactory.jsonConfig.decodeFromString<OpenAiChatStreamChunk>(data)
                        emit(chunk)
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to parse OpenAI stream chunk: $data" }
                    }
                }
            }
        }
    }
}
