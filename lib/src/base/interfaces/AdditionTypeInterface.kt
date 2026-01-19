package dev.sunriseydy.acgn.base.interfaces

import dev.sunriseydy.acgn.common.dto.AdditionalInfo

/**
 * @author SunriseYDY
 * @date 2024-07-13 01:27
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
        get() = {
            stringValueOf(it)
        }
}

interface StatusAdditionType : AdditionTypeInterface {
    override val valueOf: (List<AdditionalInfo>) -> String?
        get() = { this.stringValueOf(it) }
}