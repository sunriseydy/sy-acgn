package dev.sunriseydy.acgn.enums

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:03
 */
enum class ModuleName {
    COMMON,
    ANIME,
}

interface CommonLocalizable : Localizable {
    override val moduleName get() = ModuleName.COMMON
}

interface AnimeLocalizable : Localizable {
    override val moduleName get() = ModuleName.ANIME
}