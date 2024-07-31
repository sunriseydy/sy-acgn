package dev.sunriseydy.acgn.ui.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

/**
 * @author SunriseYDY
 * @date 2024-07-31 16:06
 */
@Composable
fun ServerConfig(setServer: (String) -> Unit) {
    Button(
        onClick = {
            setServer("test")
        }
    ) {
        Text("test")
    }
}