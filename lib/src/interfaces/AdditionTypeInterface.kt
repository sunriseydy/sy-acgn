package dev.sunriseydy.acgn.interfaces

/**
 * @author SunriseYDY
 * @date 2024-07-13 01:27
 */
interface AdditionTypeInterface<in T> : EnumLocalizable {
    val stringValueOf: (List<T>) -> String?
    val valueOf: (List<T>) -> Any?
        get() = {
            stringValueOf(it)
        }
}