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

    // 存储配置的 Map，Key 为配置键，Value 为 Pair(数据库值, 文件值)
    private val appConfigs =
        mutableMapOf<String/* config key */, Pair<AppConfig?/* db value */, String?/* file value */>>()

    /**
     * 获取配置字符串值
     *
     * 优先返回数据库配置，其次是文件配置。
     */
    fun getAppConfigStringValue(key: String): String? {
        logger.info { "Getting app config value for key: $key" }
        return appConfigs[key]?.first?.configValue ?: appConfigs[key]?.second
    }

    fun getAppConfigs() = appConfigs

    /**
     * 存储来自文件的配置
     *
     * 更新 Map 中对应 Key 的文件配置部分（Pair 的 second）。
     */
    fun putAppConfigFromFile(key: String, value: String) {
        appConfigs[key] = Pair(appConfigs[key]?.first, value)
    }

    /**
     * 存储来自数据库的配置
     *
     * 更新 Map 中对应 Key 的数据库配置部分（Pair 的 first）。
     */
    fun putAppConfigFromDB(key: String, value: AppConfig) {
        appConfigs[key] = Pair(value, appConfigs[key]?.second)
    }

    fun putAll(appConfigs: Map<String, Pair<AppConfig?, String?>>) = this.appConfigs.putAll(appConfigs)

    fun fromAppConfigList(appConfigList: List<AppConfig>) {
        appConfigList.forEach {
            this.putAppConfigFromDB(it.configKey, it)
        }
    }
}