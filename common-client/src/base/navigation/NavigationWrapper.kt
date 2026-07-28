package dev.sunriseydy.acgn.client.base.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.pages.AnimeSeason
import dev.sunriseydy.acgn.client.anime.pages.Rss
import dev.sunriseydy.acgn.client.base.api.SyAcgnApi
import dev.sunriseydy.acgn.client.base.components.SnackbarHost
import dev.sunriseydy.acgn.client.base.enums.AcgnContentType
import dev.sunriseydy.acgn.client.base.enums.AcgnNavigationContentPosition
import dev.sunriseydy.acgn.client.base.enums.LayoutType
import dev.sunriseydy.acgn.client.game.pages.GameDetailPage
import dev.sunriseydy.acgn.client.game.pages.GameListPage
import dev.sunriseydy.acgn.client.novel.pages.NovelDetailPage
import dev.sunriseydy.acgn.client.novel.pages.NovelListPage
import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig

/**
 * @author SunriseYDY
 * @date 2024-07-28 01:00
 */


/**
 * 主导航包装器
 *
 * 根据窗口大小自适应选择导航布局模式（底部导航栏或侧边导航栏）。
 * 初始化应用状态、导航动作处理器，并定义导航结构。
 *
 * @author SunriseYDY
 * @date 2024-07-28 01:00
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AcgnNavigationWrapper() {
    val adaptiveInfo = currentWindowAdaptiveInfo()

    val navigationSuiteType = if (adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600)) {
        NavigationSuiteType.NavigationDrawer
    } else {
        NavigationSuiteType.NavigationBar
    }

    val navContentPosition = if (adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(480)) {
        AcgnNavigationContentPosition.CENTER
    } else {
        AcgnNavigationContentPosition.TOP
    }

    val contentType: AcgnContentType = if (adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(840)) {
        AcgnContentType.DUAL_PANE
    } else {
        AcgnContentType.SINGLE_PANE
    }

    val navigationAction = remember {
        NavigationAction(TopLevelRouteEnum.RSS.route)
    }
    val selectedDestination = navigationAction.topLevelKey

    val snackbarHostState = remember { SnackbarHostState() }

    val appState = AppState(
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
                    navigateToTopLevelDestination = navigationAction::addTopLevel
                )

                NavigationSuiteType.NavigationDrawer -> PermanentNavigationDrawerContent(
                    selectedDestination = selectedDestination,
                    navigationContentPosition = navContentPosition,
                    navigateToTopLevelDestination = navigationAction::addTopLevel,
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surface),
                backStack = navigationAction.backStack,
                onBack = { navigationAction.removeLast() },
                entryProvider = entryProvider {
                    entry<RssRoute> {
                        Rss(appState)
                    }
                    entry<AnimeSeasonRoute> {
                        AnimeSeason(appState)
                    }
                    entry<NovelRoute> {
                        NovelListPage(appState)
                    }
                    entry<NovelDetailRoute> { route ->
                        NovelDetailPage(appState, route.novelId)
                    }
                    entry<GameRoute> {
                        GameListPage(appState)
                    }
                    entry<GameDetailRoute> { route ->
                        GameDetailPage(appState, route.gameId)
                    }
                },
            )
            SnackbarHost(
                appState = appState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/**
 * 底部导航栏组件
 *
 * @param selectedDestination 当前选中的路由
 * @param navigateToTopLevelDestination 切换顶层路由的回调函数
 */
@Composable
fun AcgnBottomNavigationBar(
    selectedDestination: NavigationRoute,
    navigateToTopLevelDestination: (NavigationRoute) -> Unit
) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        TopLevelRouteEnum.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination.route,
                onClick = { navigateToTopLevelDestination(destination.route) },
                icon = {
                    destination.route.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = destination.meaning
                        )
                    }
                },
                label = {
                    Text(
                        text = destination.meaning,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

/**
 * 永久侧边导航抽屉内容
 *
 * @param selectedDestination 当前选中的路由
 * @param navigationContentPosition 导航内容的位置（顶部或居中）
 * @param navigateToTopLevelDestination 切换顶层路由的回调函数
 * @param modifier 修饰符
 */
@Composable
fun PermanentNavigationDrawerContent(
    selectedDestination: NavigationRoute,
    navigationContentPosition: AcgnNavigationContentPosition,
    navigateToTopLevelDestination: (NavigationRoute) -> Unit,
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
    selectedDestination: NavigationRoute,
    navigateToTopLevelDestination: (NavigationRoute) -> Unit,
) = Column(
    modifier = Modifier
        .layoutId(LayoutType.CONTENT)
        .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    TopLevelRouteEnum.entries.forEach {
        NavigationDrawerItem(
            modifier = Modifier.padding(bottom = 8.dp),
            selected = selectedDestination == it.route,
            label = {
                Text(
                    text = it.meaning,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            icon = {
                it.route.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = it.meaning
                    )
                }
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            onClick = { navigateToTopLevelDestination(it.route) }
        )
    }
}


/**
 * 定义导航测量策略，确定导航内容和头部的位置及布局规则。
 *
 * @param navigationContentPosition 定义导航内容在布局中的位置，可以为顶部或居中。
 * @return 返回用于处理导航内容和头部布局的测量策略。
 */
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
