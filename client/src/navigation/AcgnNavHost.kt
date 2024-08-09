package dev.sunriseydy.acgn.client.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.sunriseydy.acgn.client.components.EmptyComingSoon
import dev.sunriseydy.acgn.client.utils.AcgnContentType
import dev.sunriseydy.acgn.client.utils.AcgnNavigationType

/**
 * @author SunriseYDY
 * @date 2024-07-28 12:28
 */
@Composable
fun AcgnNavHost(
    navController: NavHostController,
    contentType: AcgnContentType,
    navigationType: AcgnNavigationType,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AcgnNavigationRoute.RSS.name,
    ) {
        composable(AcgnNavigationRoute.RSS.name) {
            EmptyComingSoon()
        }
        composable(AcgnNavigationRoute.ANIME.name) {
            EmptyComingSoon()
        }
    }
}
