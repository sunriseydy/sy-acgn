package dev.sunriseydy.acgn.client

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

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