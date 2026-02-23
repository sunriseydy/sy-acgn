package dev.sunriseydy.acgn.client.base.enums

/**
 * 导航内容位置
 *
 * 根据设备大小和状态，定义导航内容在 Navigation Rail / Navigation Drawer 中的位置。
 */
enum class AcgnNavigationContentPosition {
    TOP, CENTER
}

/**
 * 应用内容类型
 *
 * 根据设备大小和状态，定义应用内容显示为单面板还是双面板。
 */
enum class AcgnContentType {
    SINGLE_PANE, DUAL_PANE
}

/**
 * 布局类型
 *
 * 用于导航侧边栏的自定义布局中标识头部和内容区域。
 */
enum class LayoutType {
    HEADER, CONTENT
}