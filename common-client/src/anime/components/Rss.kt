package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.sunriseydy.acgn.client.components.showError
import dev.sunriseydy.acgn.client.components.showMessage
import dev.sunriseydy.acgn.client.navigation.AppState

/**
 * @author SunriseYDY
 * @date 2024-08-11 13:23
 */
@Composable
fun Rss(appState: AppState) {
    Row {
        Button(
            onClick = {
                appState.showMessage("message")
            }
        ) {
            Text("message")
        }
        Button(
            onClick = {
                appState.showError("error")
            }
        ) {
            Text("error")
        }
    }
}