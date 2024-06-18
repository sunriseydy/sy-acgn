package dev.sunriseydy.acgn.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.BasicText
import dev.sunriseydy.acgn.getPlatform

@Composable
fun uiMain() {
    BasicText("Hello World! ${getPlatform().name}")
}