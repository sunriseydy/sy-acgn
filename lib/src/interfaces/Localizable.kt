package dev.sunriseydy.acgn.interfaces

import dev.sunriseydy.acgn.enums.ModuleName

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:02
 */
interface Localizable {
    val moduleName: ModuleName
}

interface CommonModuleLocalizable : Localizable {
    override val moduleName get() = ModuleName.COMMON
}

interface AnimeModuleLocalizable : Localizable {
    override val moduleName get() = ModuleName.ANIME
}