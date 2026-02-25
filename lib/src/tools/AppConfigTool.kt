package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.common.dto.AppConfig
import io.github.oshai.kotlinlogging.KotlinLogging
private val logger = KotlinLogging.logger { }

/**
 * 应用配置工具
 *
 * 管理应用程序的配置信息。采用双层存储结构：
 * 1. 数据库配置 (Database Config) - 优先级高
 * 2. 文件配置 (File Config) - 优先级低
 *
 * 获取配置时，优先返回数据库中的值，如果为空，则返回文件配置中的值。
 *
 * @author SunriseYDY
 * @date 2024-07-12 16:47
 */
object AppConfigTool {
    private val lock = PlatformLock()

    /** 存储配置快照，Key 为配置键，Value 为 Pair(数据库值, 文件值) */
    private var appConfigs: Map<String, Pair<AppConfig?, String?>> = emptyMap()

    /**
     * 获取配置字符串值
     *
     * 优先返回数据库配置，其次是文件配置。
     *
     * @param key 配置键
     * @return 配置值字符串，如果不存在则返回 null
     */
    fun getAppConfigStringValue(key: String): String? {
        return lock.withLock {
            logger.info { "获取配置值: key=$key" }
            appConfigs[key]?.let { (dbConfig, fileValue) ->
                dbConfig?.configValue ?: fileValue
            }
        }
    }

    /**
     * 获取所有配置的只读视图
     */
    fun getAppConfigs(): Map<String, Pair<AppConfig?, String?>> = lock.withLock { appConfigs.toMap() }

    /**
     * 存储来自文件的配置
     *
     * 更新 Map 中对应 Key 的文件配置部分（Pair 的 second）。
     *
     * @param key 配置键
     * @param value 文件配置值
     */
    fun putAppConfigFromFile(key: String, value: String) {
        lock.withLock {
            val current = appConfigs[key]
            val newPair = if (current != null) Pair(current.first, value) else Pair(null, value)
            appConfigs = appConfigs + (key to newPair)
        }
    }

    /**
     * 存储来自数据库的配置
     *
     * 更新 Map 中对应 Key 的数据库配置部分（Pair 的 first）。
     *
     * @param key 配置键
     * @param value 数据库配置对象
     */
    fun putAppConfigFromDB(key: String, value: AppConfig) {
        lock.withLock {
            val current = appConfigs[key]
            val newPair = if (current != null) Pair(value, current.second) else Pair(value, null)
            appConfigs = appConfigs + (key to newPair)
        }
    }

    /**
     * 批量添加配置
     */
    fun putAll(configs: Map<String, Pair<AppConfig?, String?>>) {
        lock.withLock {
            appConfigs = appConfigs + configs
        }
    }

    /**
     * 从数据库配置列表批量加载配置
     *
     * @param appConfigList 数据库配置列表
     */
    fun fromAppConfigList(appConfigList: List<AppConfig>) {
        lock.withLock {
            var updated = appConfigs
            appConfigList.forEach {
                val current = updated[it.configKey]
                updated = updated + (it.configKey to Pair(it, current?.second))
            }
            appConfigs = updated
        }
    }
}
