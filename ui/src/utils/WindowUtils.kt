package dev.sunriseydy.acgn.ui.utils

/**
 * @author SunriseYDY
 * @date 2024-07-28 00:35
 */

fun isCompact() = false
fun isMedium() = false
fun isLarge() = true

fun getContentType() = if (isCompact() || isMedium()) {
    AcgnContentType.SINGLE_PANE
} else if (isLarge()) {
    AcgnContentType.DUAL_PANE
} else {
    AcgnContentType.SINGLE_PANE
}

fun getNavigationType() = if (isCompact()) {
    AcgnNavigationType.BOTTOM_NAVIGATION
} else if (isMedium() || isLarge()) {
    AcgnNavigationType.PERMANENT_NAVIGATION_DRAWER
} else {
    AcgnNavigationType.BOTTOM_NAVIGATION
}

fun getNavigationContentPosition() = if (isCompact()) {
    AcgnNavigationContentPosition.TOP
} else if (isMedium() || isLarge()) {
    AcgnNavigationContentPosition.CENTER
} else {
    AcgnNavigationContentPosition.TOP
}

/**
 * Different type of navigation supported by app depending on device size and state.
 */
enum class AcgnNavigationType {
    BOTTOM_NAVIGATION, NAVIGATION_RAIL, PERMANENT_NAVIGATION_DRAWER
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