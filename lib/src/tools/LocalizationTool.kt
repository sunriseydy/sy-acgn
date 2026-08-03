package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.base.enums.Language
import io.github.oshai.kotlinlogging.KotlinLogging
private val logger = KotlinLogging.logger { }

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
    private val lock = PlatformLock()

    val DEFAULT_LANGUAGE = Language.SIMPLIFIED_CHINESE

    private var currentLanguageState = DEFAULT_LANGUAGE
    var currentLanguage: Language
        get() = lock.withLock { currentLanguageState }
        set(value) {
            lock.withLock { currentLanguageState = value }
        }

    /** 存储本地化键值对快照 */
    private var localizations: Map<String, String> = emptyMap()

    /**
     * 批量添加本地化字符串
     */
    fun putAll(localizations: Map<String, String>) {
        lock.withLock {
            this.localizations += localizations
        }
    }

    /**
     * 添加单个本地化字符串
     */
    fun putLocalization(key: String, value: String) {
        lock.withLock {
            localizations = localizations + (key to value)
        }
    }

    /**
     * 用新的语言和本地化字典进行原子替换，避免并发加载时出现混合状态。
     */
    fun replaceAll(language: Language, localizations: Map<String, String>) {
        lock.withLock {
            currentLanguageState = language
            this.localizations = localizations.toMap()
        }
    }

    fun getLocalizations(): Map<String, String> = lock.withLock { localizations.toMap() }

    /**
     * 获取本地化字符串
     *
     * @param key 本地化键
     * @param defaultValue 默认值，如果未找到则返回此值（默认返回键本身）
     * @return 本地化字符串或默认值
     */
    fun getLocalization(key: String, defaultValue: String = key): String {
        val snapshot = lock.withLock {
            Pair(localizations[key], currentLanguageState)
        }
        return snapshot.first ?: run {
            logger.warn { "本地化键 [$key] 在当前语言 [${snapshot.second.originName}] 下未找到对应的翻译" }
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
