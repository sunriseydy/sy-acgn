package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.common.dto.AppConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger { }

/**
 * 应用配置工具
 *
 * 管理应用程序的配置信息。采用双层存储结构：
 * 1. 数据库配置 (Database Config) - 优先级高
 * 2. 文件配置 (File Config) - 优先级低
 *
 * 获取配置时，优先返回数据库中的值，如果为空，则返回文件配置中的值。
 * 使用 [ConcurrentHashMap] 保证线程安全。
 *
 * @author SunriseYDY
 * @date 2024-07-12 16:47
 */
object AppConfigTool {

    /** 存储配置的线程安全 Map，Key 为配置键，Value 为 Pair(数据库值, 文件值) */
    private val appConfigs = ConcurrentHashMap<String, Pair<AppConfig?, String?>>()

    /**
     * 获取配置字符串值
     *
     * 优先返回数据库配置，其次是文件配置。
     *
     * @param key 配置键
     * @return 配置值字符串，如果不存在则返回 null
     */
    fun getAppConfigStringValue(key: String): String? {
        logger.info { "获取配置值: key=$key" }
        return appConfigs[key]?.let { (dbConfig, fileValue) ->
            dbConfig?.configValue ?: fileValue
        }
    }

    /**
     * 获取所有配置的只读视图
     */
    fun getAppConfigs(): Map<String, Pair<AppConfig?, String?>> = appConfigs

    /**
     * 存储来自文件的配置
     *
     * 更新 Map 中对应 Key 的文件配置部分（Pair 的 second）。
     *
     * @param key 配置键
     * @param value 文件配置值
     */
    fun putAppConfigFromFile(key: String, value: String) {
        appConfigs.merge(key, Pair(null, value)) { existing, _ ->
            Pair(existing.first, value)
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
        appConfigs.merge(key, Pair(value, null)) { existing, _ ->
            Pair(value, existing.second)
        }
    }

    /**
     * 批量添加配置
     */
    fun putAll(appConfigs: Map<String, Pair<AppConfig?, String?>>) = this.appConfigs.putAll(appConfigs)

    /**
     * 从数据库配置列表批量加载配置
     *
     * @param appConfigList 数据库配置列表
     */
    fun fromAppConfigList(appConfigList: List<AppConfig>) {
        appConfigList.forEach { putAppConfigFromDB(it.configKey, it) }
    }
}