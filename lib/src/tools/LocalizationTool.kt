package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.base.enums.Language


/**
 * 本地化工具
 *
 * 管理应用程序的本地化字符串资源。
 * 支持设置当前语言，存储和获取键值对。
 *
 * @author SunriseYDY
 * @date 2024-07-09 14:56
 */
object LocalizationTool {
    val DEFAULT_LANGUAGE = Language.SIMPLIFIED_CHINESE
    var currentLanguage = DEFAULT_LANGUAGE

    // 存储本地化键值对的 Map
    private val localizations = mutableMapOf<String, String>()

    /**
     * 批量添加本地化字符串
     */
    fun putAll(localizations: Map<String, String>) = this.localizations.putAll(localizations)

    /**
     * 添加单个本地化字符串
     */
    fun putLocalization(key: String, value: String) {
        localizations[key] = value
    }

    fun getLocalizations() = localizations

    /**
     * 获取本地化字符串
     *
     * @param key 本地化键
     * @param defaultValue 默认值，如果未找到则返回此值（默认返回键本身）
     * @return 本地化字符串或默认值
     */
    fun getLocalization(key: String, defaultValue: String = key): String {
        return localizations.getOrElse(key) {
            println("the key [$key] on current language [${currentLanguage.originName}] doesn't have a localization")
            defaultValue
        }
    }
}

/**
 * 本地化简写函数
 *
 * @see LocalizationTool.getLocalization
 */
fun i(key: String, defaultValue: String = key) = LocalizationTool.getLocalization(key, defaultValue)