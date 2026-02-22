package dev.sunriseydy.acgn.base.interfaces

import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * 配置项 公共接口
 * @author SunriseYDY
 * @date 2024-07-12 15:07
 */
interface AppConfigInterface : Key {
    val configKey get() = "config.${this.moduleName}.${this::class.simpleName}"
    override val key get() = "${this.configKey}.meaning"
    val descriptionKey get() = "${this.configKey}.description"
    val description get() = LocalizationTool.getLocalization(this.descriptionKey)

    val stringValue: String? get() = AppConfigTool.getAppConfigStringValue(this.configKey)

    val configValue: Any?
}

/**
 * 字符串类型配置项基类
 *
 * 所有值为 String? 的 配置项的公共父类，避免重复定义 [configValue]。
 */
interface StringAppConfig : AppConfigInterface {
    override val configValue: String? get() = this.stringValue
}