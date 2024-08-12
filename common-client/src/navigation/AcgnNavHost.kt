package dev.sunriseydy.acgn.client.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.components.Rss
import dev.sunriseydy.acgn.client.components.EmptyComingSoon

/**
 * @author SunriseYDY
 * @date 2024-07-28 12:28
 */
@Composable
fun AcgnNavHost(
    appState: AppState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = appState.navController,
        startDestination = AcgnNavigationRoute.RSS.name,
    ) {
        composable(AcgnNavigationRoute.RSS.name) {
            Rss(appState)
        }
        composable(AcgnNavigationRoute.ANIME.name) {
            EmptyComingSoon()
        }
    }
}
