package dev.sunriseydy.acgn.interfaces

import dev.sunriseydy.acgn.enums.ModuleName
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-09 15:02
 */
interface Localizable {
    val moduleName: ModuleName
    val localizationKey: String
    val localization: String get() = LocalizationTool.getLocalization(this.localizationKey)
}

interface CommonModuleLocalizable : Localizable {
    override val moduleName get() = ModuleName.COMMON
}

interface AnimeModuleLocalizable : Localizable {
    override val moduleName get() = ModuleName.ANIME
}

interface EnumLocalizable : Localizable {
    override val localizationKey get() = LocalizationTool.getLocalizationKeyFromEnum(this)
}