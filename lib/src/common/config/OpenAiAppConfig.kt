package dev.sunriseydy.acgn.common.config

import dev.sunriseydy.acgn.base.interfaces.CommonModule
import dev.sunriseydy.acgn.base.interfaces.StringAppConfig

/**
 * OpenAI 兼容 API 配置常量
 *
 * @author SunriseYDY
 * @date 2026-10-17
 */
object OpenAiAppConfig {

    /** OpenAI 兼容端点 URL（必填） */
    object OpenAiEndpoint : StringAppConfig, CommonModule

    /** OpenAI 访问密钥（必填） */
    object OpenAiApiKey : StringAppConfig, CommonModule

    /** 大模型名称（必填，无默认值） */
    object OpenAiModel : StringAppConfig, CommonModule
}
