package dev.sunriseydy.acgn.client.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.toSize
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.LayoutType
import dev.sunriseydy.acgn.client.SyAcgnApi
import dev.sunriseydy.acgn.client.components.AcgnSnackbarHost
import dev.sunriseydy.acgn.client.utils.*
import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig

/**
 * @author SunriseYDY
 * @date 2024-07-28 01:00
 */


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AcgnNavigationWrapper() {
    val adaptiveInfo = currentWindowAdaptiveInfo()

    val navigationType = when {
        adaptiveInfo.windowSizeClass.isCompact() -> AcgnNavigationType.BOTTOM_NAVIGATION
        else -> AcgnNavigationType.PERMANENT_NAVIGATION_DRAWER
    }
    val navigationSuiteType = when {
        adaptiveInfo.windowSizeClass.isCompact() -> NavigationSuiteType.NavigationBar
        else -> NavigationSuiteType.NavigationDrawer
    }
    val navContentPosition = when (adaptiveInfo.windowSizeClass.windowHeightSizeClass) {
        WindowHeightSizeClass.COMPACT -> AcgnNavigationContentPosition.TOP
        else -> AcgnNavigationContentPosition.CENTER
    }
    val contentType: AcgnContentType = when (adaptiveInfo.windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT, WindowWidthSizeClass.MEDIUM -> AcgnContentType.SINGLE_PANE
        else -> AcgnContentType.DUAL_PANE
    }

    val navController = rememberNavController()
    val navigationAction = remember(navController) {
        AcgnNavigationAction(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination =
        navBackStackEntry?.destination?.route ?: AcgnNavigationRoute.RSS.name

    // ydy todo
    val snackbarHostState = remember { SnackbarHostState() }

    val appState = AppState(
        navController = navController,
        navigationAction = navigationAction,
        snackbarHostState = snackbarHostState,
        scope = rememberCoroutineScope(),
        contentType = contentType,
        api = SyAcgnApi()
    )

    NavigationSuiteScaffoldLayout(
        layoutType = navigationSuiteType,
        navigationSuite = {
            when (navigationSuiteType) {
                NavigationSuiteType.NavigationBar -> AcgnBottomNavigationBar(
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigationAction::navigateTo
                )

                NavigationSuiteType.NavigationDrawer -> PermanentNavigationDrawerContent(
                    selectedDestination = selectedDestination,
                    navigationContentPosition = navContentPosition,
                    navigateToTopLevelDestination = navigationAction::navigateTo,
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    ) {
        AcgnNavHost(
            appState = appState,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
fun AcgnBottomNavigationBar(
    selectedDestination: String,
    navigateToTopLevelDestination: (AcgnNavigationRoute) -> Unit
) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        AcgnNavigationRoute.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination.name,
                onClick = { navigateToTopLevelDestination(destination) },
                icon = {
                    destination.icon?.apply {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.localization
                        )
                    }
                },
                label = {
                    if (destination.icon == null) {
                        Text(
                            text = destination.localization,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun PermanentNavigationDrawerContent(
    selectedDestination: String,
    navigationContentPosition: AcgnNavigationContentPosition,
    navigateToTopLevelDestination: (AcgnNavigationRoute) -> Unit,
    modifier: Modifier
) {
    PermanentDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Layout(
            content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = CommonModuleAppConfig.AppName.configValue.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                NavigationDrawerItems(selectedDestination, navigateToTopLevelDestination)
            },
            measurePolicy = navigationMeasurePolicy(navigationContentPosition)
        )
    }
}

@Composable
fun NavigationDrawerItems(
    selectedDestination: String,
    navigateToTopLevelDestination: (AcgnNavigationRoute) -> Unit,
) = Column(
    modifier = Modifier
        .layoutId(LayoutType.CONTENT)
        .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    AcgnNavigationRoute.entries.forEach { destination ->
        NavigationDrawerItem(
            modifier = Modifier.padding(bottom = 8.dp),
            selected = selectedDestination == destination.name,
            label = {
                Text(
                    text = destination.localization,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            icon = {
                destination.icon?.apply {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.localization
                    )
                }
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            onClick = { navigateToTopLevelDestination(destination) }
        )
    }
}

fun navigationMeasurePolicy(
    navigationContentPosition: AcgnNavigationContentPosition,
): MeasurePolicy = MeasurePolicy { measurables, constraints ->
    lateinit var headerMeasurable: Measurable
    lateinit var contentMeasurable: Measurable
    measurables.forEach {
        when (it.layoutId) {
            LayoutType.HEADER -> headerMeasurable = it
            LayoutType.CONTENT -> contentMeasurable = it
            else -> error("Unknown layoutId encountered!")
        }
    }

    val headerPlaceable = headerMeasurable.measure(constraints)
    val contentPlaceable = contentMeasurable.measure(
        constraints.offset(vertical = -headerPlaceable.height)
    )
    layout(constraints.maxWidth, constraints.maxHeight) {
        // Place the header, this goes at the top
        headerPlaceable.placeRelative(0, 0)

        // Determine how much space is not taken up by the content
        val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

        val contentPlaceableY = when (navigationContentPosition) {
            // Figure out the place we want to place the content, with respect to the
            // parent (ignoring the header for now)
            AcgnNavigationContentPosition.TOP -> 0
            AcgnNavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
        }
            // And finally, make sure we don't overlap with the header.
            .coerceAtLeast(headerPlaceable.height)

        contentPlaceable.placeRelative(0, contentPlaceableY)
    }
}