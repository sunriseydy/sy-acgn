package dev.sunriseydy.acgn.enums

import dev.sunriseydy.acgn.interfaces.CommonModule
import dev.sunriseydy.acgn.interfaces.EnumKey

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:38
 */
enum class Language(val languageCode: String, val regionalCode: String, val originName: String) :
    CommonModule, EnumKey {
    SIMPLIFIED_CHINESE("zh", "CN", "简体中文"),
    ENGLISH("en", "US", "English"),
}