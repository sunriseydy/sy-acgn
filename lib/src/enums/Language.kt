package dev.sunriseydy.acgn.enums

import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable
import dev.sunriseydy.acgn.interfaces.EnumLocalizable

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:38
 */
enum class Language(val languageCode: String, val regionalCode: String, val originName: String) :
    CommonModuleLocalizable, EnumLocalizable {
    SIMPLIFIED_CHINESE("zh", "CN", "简体中文"),
    ENGLISH("en", "US", "English"),
}