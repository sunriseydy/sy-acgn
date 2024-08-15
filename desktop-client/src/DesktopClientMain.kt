package dev.sunriseydy.acgn.client

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState: WindowState = rememberWindowState(
        placement = WindowPlacement.Maximized,
        size = DpSize(width = 1200.dp, height = 1000.dp),
    )
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "SY ACGN",
    ) {
        App()
    }
}