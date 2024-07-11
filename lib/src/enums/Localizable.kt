package dev.sunriseydy.acgn.enums

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:02
 */
interface Localizable {
    val moduleName: ModuleName
}

interface CommonLocalizable : Localizable {
    override val moduleName get() = ModuleName.COMMON
}

interface AnimeLocalizable : Localizable {
    override val moduleName get() = ModuleName.ANIME
}