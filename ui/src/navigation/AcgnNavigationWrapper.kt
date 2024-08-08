package dev.sunriseydy.acgn.ui.navigation

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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig
import dev.sunriseydy.acgn.ui.utils.AcgnNavigationContentPosition
import dev.sunriseydy.acgn.ui.utils.AcgnNavigationType
import dev.sunriseydy.acgn.ui.utils.getContentType
import dev.sunriseydy.acgn.ui.utils.getNavigationContentPosition
import dev.sunriseydy.acgn.ui.utils.getNavigationType

/**
 * @author SunriseYDY
 * @date 2024-07-28 01:00
 */


@Composable
fun AcgnNavigationWrapper() {
    val navigationType = getNavigationType()
    val navContentPosition = getNavigationContentPosition()
    val contentType = getContentType()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    // Avoid opening the modal drawer when there is a permanent drawer or a bottom nav bar,
    // but always allow closing an open drawer.
    val gesturesEnabled =
        drawerState.isOpen || navigationType == AcgnNavigationType.NAVIGATION_RAIL

    val navController = rememberNavController()
    val navigationAction = remember(navController) {
        AcgnNavigationAction(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination =
        navBackStackEntry?.destination?.route ?: AcgnNavigationRoute.RSS.name

    when (navigationType) {
        AcgnNavigationType.BOTTOM_NAVIGATION -> Scaffold(
            bottomBar = {
                AcgnBottomNavigationBar(
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigationAction::navigateTo
                )
            },
            content = { contentPadding ->
                AcgnNavHost(
                    navController = navController,
                    contentType = contentType,
                    navigationType = navigationType,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                )
            }
        )

        AcgnNavigationType.PERMANENT_NAVIGATION_DRAWER -> PermanentNavigationDrawer(
            drawerContent = {
                PermanentNavigationDrawerContent(
                    selectedDestination = selectedDestination,
                    navigationContentPosition = navContentPosition,
                    navigateToTopLevelDestination = navigationAction::navigateTo,
                    modifier = Modifier.width(150.dp)
                )
            },
            content = {
                AcgnNavHost(
                    navController = navController,
                    contentType = contentType,
                    navigationType = navigationType,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surface)
                )
            }
        )

        else -> throw IllegalArgumentException("Invalid navigation type")
    }
}

@Composable
fun AcgnNavigationRail(
    selectedDestination: String,
    navigationContentPosition: AcgnNavigationContentPosition,
    navigateToTopLevelDestination: (AcgnNavigationRoute) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
        header = {
            Column(
                modifier = Modifier.layoutId(LayoutType.HEADER),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NavigationRailItem(
                    selected = false,
                    onClick = onDrawerClicked,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.layoutId(LayoutType.CONTENT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AcgnNavigationRoute.entries.forEach { destination ->
                NavigationRailItem(
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
fun ModalNavigationDrawerContent(
    selectedDestination: String,
    navigationContentPosition: AcgnNavigationContentPosition,
    navigateToTopLevelDestination: (AcgnNavigationRoute) -> Unit,
    onDrawerClicked: () -> Unit = {}
) {
    ModalDrawerSheet {
        Layout(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.inverseOnSurface)
                .padding(16.dp),
            content = {
                Column(
                    modifier = Modifier.layoutId(LayoutType.HEADER),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CommonModuleAppConfig.AppName.configValue.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDrawerClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                                contentDescription = null
                            )
                        }
                    }
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

enum class LayoutType {
    HEADER, CONTENT
}
