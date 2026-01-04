package dev.sunriseydy.acgn.client.utils

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * @author SunriseYDY
 * @date 2024-07-28 00:35
 */

fun WindowSizeClass.isCompact() = windowWidthSizeClass == WindowWidthSizeClass.COMPACT

/**
 * Different type of navigation supported by app depending on device size and state.
 */
enum class AcgnNavigationType {
    BOTTOM_NAVIGATION, PERMANENT_NAVIGATION_DRAWER
}

/**
 * Different position of navigation content inside Navigation Rail, Navigation Drawer depending on device size and state.
 */
enum class AcgnNavigationContentPosition {
    TOP, CENTER
}

/**
 * App Content shown depending on device size and state.
 */
enum class AcgnContentType {
    SINGLE_PANE, DUAL_PANE
}