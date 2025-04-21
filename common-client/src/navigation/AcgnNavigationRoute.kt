package dev.sunriseydy.acgn.client.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import dev.sunriseydy.acgn.interfaces.CommonModule
import dev.sunriseydy.acgn.interfaces.EnumKey

/**
 * @author SunriseYDY
 * @date 2024-07-27 22:11
 */

enum class AcgnNavigationRoute(val icon: ImageVector? = null) : CommonModule, EnumKey {
    RSS,
    ANIME_SEASON,
}
