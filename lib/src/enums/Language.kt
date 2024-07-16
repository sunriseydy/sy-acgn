package dev.sunriseydy.acgn.enums

import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable
import dev.sunriseydy.acgn.interfaces.EnumLocalizable

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:38
 */
enum class Language(val code: String, val language: String) : CommonModuleLocalizable, EnumLocalizable {
    SIMPLIFIED_CHINESE("zh_CN", "zh-CN"),
}