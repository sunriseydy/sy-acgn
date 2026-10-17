package dev.sunriseydy.acgn.server.common.service

import dev.sunriseydy.acgn.common.dto.OpenAiChatRequest
import dev.sunriseydy.acgn.common.dto.OpenAiChatStreamChunk
import kotlinx.coroutines.flow.Flow

/**
 * OpenAI 业务服务接口
 *
 * @author SunriseYDY
 * @date 2026-10-17
 */
interface OpenAiService {

    /**
     * 发起流式对话
     *
     * @param request 对话请求
     * @return 流式响应数据块 [Flow]
     */
    suspend fun chatStream(request: OpenAiChatRequest): Flow<OpenAiChatStreamChunk>
}
