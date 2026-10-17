package dev.sunriseydy.acgn.common.dto

import kotlinx.serialization.Serializable

/**
 * OpenAI 对话消息
 *
 * @property role 角色，如 "system", "user", "assistant"
 * @property content 消息文本内容
 * @property name 参与者可选名称
 */
@Serializable
data class OpenAiChatMessage(
    val role: String,
    val content: String,
    val name: String? = null,
)

/**
 * OpenAI 对话请求
 *
 * @property messages 消息上下文列表
 * @property model 模型名称，若未指定则由服务端自动注入配置中的模型
 * @property temperature 采样温度
 * @property stream 是否流式输出，默认为 true
 */
@Serializable
data class OpenAiChatRequest(
    val messages: List<OpenAiChatMessage>,
    val model: String? = null,
    val temperature: Double? = null,
    val stream: Boolean = true,
) {
    companion object {
        /**
         * 快速构造简单对话请求
         */
        fun simple(
            prompt: String,
            systemPrompt: String? = null,
            model: String? = null,
            temperature: Double? = null,
        ) = OpenAiChatRequest(
            messages = buildList {
                if (!systemPrompt.isNullOrBlank()) {
                    add(OpenAiChatMessage(role = "system", content = systemPrompt))
                }
                add(OpenAiChatMessage(role = "user", content = prompt))
            },
            model = model,
            temperature = temperature,
            stream = true,
        )
    }
}

/**
 * 流式响应中的增量内容
 */
@Serializable
data class OpenAiChatDelta(
    val role: String? = null,
    val content: String? = null,
)

/**
 * 流式响应中的选项
 */
@Serializable
data class OpenAiChatStreamChoice(
    val index: Int = 0,
    val delta: OpenAiChatDelta? = null,
    val finish_reason: String? = null,
)

/**
 * OpenAI 流式响应 Chunk
 */
@Serializable
data class OpenAiChatStreamChunk(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<OpenAiChatStreamChoice> = emptyList(),
) {
    /**
     * 快捷获取该 Chunk 中的文本增量
     */
    val textDelta: String get() = choices.firstOrNull()?.delta?.content ?: ""
}
