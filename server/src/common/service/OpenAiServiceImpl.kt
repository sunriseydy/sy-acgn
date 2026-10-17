package dev.sunriseydy.acgn.server.common.service

import dev.sunriseydy.acgn.base.exception.MessageException
import dev.sunriseydy.acgn.common.config.OpenAiAppConfig
import dev.sunriseydy.acgn.common.dto.OpenAiChatRequest
import dev.sunriseydy.acgn.common.dto.OpenAiChatStreamChunk
import dev.sunriseydy.acgn.common.enums.CommonModuleError
import dev.sunriseydy.acgn.server.common.tools.OpenAiTool
import kotlinx.coroutines.flow.Flow

/**
 * OpenAI 业务服务实现
 *
 * 负责强校验端点、密钥与模型（均必填，无默认值），并委派 [OpenAiTool] 发起流式调用。
 *
 * @author SunriseYDY
 * @date 2026-10-17
 */
class OpenAiServiceImpl(
    private val openAiTool: OpenAiTool
) : OpenAiService {

    override suspend fun chatStream(request: OpenAiChatRequest): Flow<OpenAiChatStreamChunk> {
        val endpoint = OpenAiAppConfig.OpenAiEndpoint.configValue?.takeIf { it.isNotBlank() }
            ?: throw MessageException(CommonModuleError.OPENAI_CONFIG_ENDPOINT_MISSING)

        val apiKey = OpenAiAppConfig.OpenAiApiKey.configValue?.takeIf { it.isNotBlank() }
            ?: throw MessageException(CommonModuleError.OPENAI_CONFIG_API_KEY_MISSING)

        val model = (request.model?.takeIf { it.isNotBlank() }
            ?: OpenAiAppConfig.OpenAiModel.configValue?.takeIf { it.isNotBlank() })
            ?: throw MessageException(CommonModuleError.OPENAI_CONFIG_MODEL_MISSING)

        return openAiTool.chatStream(
            request = request,
            endpoint = endpoint,
            apiKey = apiKey,
            model = model
        )
    }
}
