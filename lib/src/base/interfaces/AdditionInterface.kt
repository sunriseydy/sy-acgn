package dev.sunriseydy.acgn.base.interfaces

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * 附加信息接口
 *
 * 表示一个实体可以拥有的附加信息属性。
 * 该接口提供了 `additions` 属性用于存储附加信息列表。
 */
interface AdditionInterface {
    /**
     * 附加信息列表
     */
    val additions: List<AdditionalInfo>
}

/**
 * 表示附加信息类型接口，用于定义和处理特定类型的附加信息。
 *
 * 此接口继承自 [Key] 接口，主要提供了几个便于从附加信息列表中提取数据的扩展属性。
 *
 * 特性包括：
 * 1. 自动生成特定键值 `key`，格式为 `"addition.{moduleName}.{类型名称}"`。
 * 2. 提供从 [AdditionalInfo] 列表中解析和提取特定信息的接口。
 *
 * 主要功能：
 * - 根据类型键值从附加信息列表中找到第一个匹配的 [AdditionalInfo]。
 * - 提取附加信息中的具体字符串值。
 * - 支持扩展以通过子类自定义提取方式。
 */
interface AdditionTypeInterface : Key {
    override val key get() = "addition.${this.moduleName}.${this::class.simpleName}"
    val additionalInfo: (List<AdditionalInfo>) -> AdditionalInfo?
        get() = {
            it.find { addition -> addition.additionalType == this.key }
        }
    val stringValueOf: (List<AdditionalInfo>) -> String?
        get() = {
            additionalInfo(it)?.additionalValue
        }
    val valueOf: (List<AdditionalInfo>) -> Any?
}

/**
 * 表示字符串附加类型的接口，用于从附加信息列表中提取字符串值。
 *
 * 此接口继承自 [AdditionTypeInterface]，并复写其 `valueOf` 属性，
 * 使其默认实现为使用 `stringValueOf` 方法提取字符串值。
 *
 * 主要功能：
 * - 定义特定的字符串附加类型标识。
 * - 提供便捷的方式从 [AdditionalInfo] 列表中提取字符串值。
 */
interface StringAdditionType : AdditionTypeInterface {
    override val valueOf: (List<AdditionalInfo>) -> String?
        get() = { this.stringValueOf(it) }
}


/**
 * 表示一个接口，用于处理以 JSON 对象形式表示的附加信息类型。
 *
 * 该接口继承自 [AdditionTypeInterface]，并通过重写 `valueOf` 属性，提供了从 [AdditionalInfo] 列表中解析并转换为
 * [JsonObject] 的功能。
 *
 * 功能描述：
 * - 利用父类的 `stringValueOf` 方法从给定的 [AdditionalInfo] 列表中提取附加信息的字符串值。
 * - 将提取到的字符串值解析为 JSON 元素，并转换为 [JsonObject]。
 *
 * 属性:
 * - `valueOf` 通过接受一个 [List] 类型的 [AdditionalInfo] 参数，返回对应的 [JsonObject] (如果有可用的数据)。
 *   如果无法提取有效的字符串值或解析失败，则返回 `null`。
 */
interface JsonObjectAdditionTypeInterface : AdditionTypeInterface {
    override val valueOf: (List<AdditionalInfo>) -> JsonObject?
        get() = {
            this.stringValueOf(it)?.let { json -> Json.parseToJsonElement(json).jsonObject }
        }
}