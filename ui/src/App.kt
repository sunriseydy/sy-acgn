package dev.sunriseydy.acgn.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import dev.sunriseydy.acgn.ui.navigation.AcgnNavigationWrapper

@Composable
fun App() {
    MaterialTheme {
        Surface {
            AcgnNavigationWrapper()
        }
    }
}