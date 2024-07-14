package dev.sunriseydy.acgn.interfaces

import dev.sunriseydy.acgn.common.dto.AdditionalInfo

/**
 * @author SunriseYDY
 * @date 2024-07-13 01:27
 */
interface AdditionTypeInterface : Localizable {
    override val localizationKey get() = "addition.${this.moduleName}.${this::class.simpleName}"
    val key: String get() = this.localizationKey
    val stringValueOf: (List<AdditionalInfo>) -> String?
        get() = {
            it.find { addition -> addition.additionalType == this.key }?.additionalValue
        }
    val valueOf: (List<AdditionalInfo>) -> Any?
        get() = {
            stringValueOf(it)
        }
}