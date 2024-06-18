package dev.sunriseydy.acgn.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SY ACGN"
    ) {
        App()
    }
}