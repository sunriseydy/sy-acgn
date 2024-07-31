package dev.sunriseydy.acgn.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import dev.sunriseydy.acgn.interfaces.CommonModuleLocalizable
import dev.sunriseydy.acgn.interfaces.EnumLocalizable

/**
 * @author SunriseYDY
 * @date 2024-07-27 22:11
 */

enum class AcgnNavigationRoute(val icon: ImageVector? = null) : CommonModuleLocalizable, EnumLocalizable {
    RSS,
    ANIME,
}
