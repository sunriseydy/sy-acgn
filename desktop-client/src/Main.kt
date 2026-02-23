package dev.sunriseydy.acgn.client

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

/**
 * 桌面应用入口点
 *
 * 初始化单窗口应用程序，设置窗口标题、最大化状态和默认大小。
 * 默认窗口大小为 1200x1000 dp，启动时自动最大化。
 */
fun main() =
    singleWindowApplication(
        title = "SY ACGN",
        state = WindowState(
            placement = WindowPlacement.Maximized,
            size = DpSize(width = 1200.dp, height = 1000.dp),
        ),
    ) {
        App()
    }