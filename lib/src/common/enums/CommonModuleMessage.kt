package dev.sunriseydy.acgn.common.enums

import dev.sunriseydy.acgn.base.interfaces.CommonModule
import dev.sunriseydy.acgn.base.interfaces.ErrorMessage

/**
 * @author SunriseYDY
 * @date 2025-04-21 01:10
 */
enum class CommonModuleError : CommonModule, ErrorMessage {
    TEST,
    OPENAI_CONFIG_ENDPOINT_MISSING,
    OPENAI_CONFIG_API_KEY_MISSING,
    OPENAI_CONFIG_MODEL_MISSING,
    OPENAI_REQUEST_FAILED,
}