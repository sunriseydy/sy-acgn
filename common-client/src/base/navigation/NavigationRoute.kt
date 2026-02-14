package dev.sunriseydy.acgn.client.base.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import dev.sunriseydy.acgn.base.interfaces.CommonModule
import dev.sunriseydy.acgn.base.interfaces.EnumKey

/**
 * 导航路由接口
 *
 * 定义所有导航路由必须实现的属性。目前主要包含可选的图标。
 * TODO: 考虑更好的模块化方案
 *
 * @author SunriseYDY
 * @date 2024-07-27 22:11
 */
sealed interface NavigationRoute {
    val icon: ImageVector?
}

/**
 * RSS 页面路由
 */
data object RssRoute : NavigationRoute {
    override val icon: ImageVector? = null
}

/**
 * 动漫季度页面路由
 */
data object AnimeSeasonRoute : NavigationRoute {
    override val icon: ImageVector? = null
}

/**
 * 顶层路由枚举
 *
 * 定义应用的顶层导航项，同时实现了 CommonModule 和 EnumKey 接口以支持本地化。
 */
enum class TopLevelRouteEnum(val route: NavigationRoute) : CommonModule, EnumKey {
    RSS(route = RssRoute),
    ANIME_SEASON(route = AnimeSeasonRoute),
}