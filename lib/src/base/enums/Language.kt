package dev.sunriseydy.acgn.base.enums

import dev.sunriseydy.acgn.base.interfaces.CommonModule
import dev.sunriseydy.acgn.base.interfaces.EnumKey

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:38
 */
enum class Language(val languageCode: String, val regionalCode: String, val originName: String) :
    CommonModule, EnumKey {
    SIMPLIFIED_CHINESE("zh", "CN", "简体中文"),
    ENGLISH("en", "US", "English"),
}