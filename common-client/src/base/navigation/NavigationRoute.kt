package dev.sunriseydy.acgn.client.base.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import dev.sunriseydy.acgn.base.interfaces.CommonModule
import dev.sunriseydy.acgn.base.interfaces.EnumKey

/**
 * 导航路由 todo 模块化
 * @author SunriseYDY
 * @date 2024-07-27 22:11
 */
sealed interface NavigationRoute {
    val icon: ImageVector?
}

data object RssRoute : NavigationRoute {
    override val icon: ImageVector? = null
}

data object AnimeSeasonRoute : NavigationRoute {
    override val icon: ImageVector? = null
}

enum class TopLevelRouteEnum(val route: NavigationRoute) : CommonModule, EnumKey {
    RSS(route = RssRoute),
    ANIME_SEASON(route = AnimeSeasonRoute),
}