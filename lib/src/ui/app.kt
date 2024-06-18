package dev.sunriseydy.acgn.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.BasicText
import dev.sunriseydy.acgn.getPlatform

@Composable
fun app() {
    BasicText("Hello World! ${getPlatform()}")
}