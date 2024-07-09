package dev.sunriseydy.acgn.enums

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:02
 */
interface Localizable {
    val moduleName: ModuleName
    val enumName: String

    /**
     * enum name prefix, if null will use enum name
     */
    var prefix: String?
}