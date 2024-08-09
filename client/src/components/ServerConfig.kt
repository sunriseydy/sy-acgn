package dev.sunriseydy.acgn.client.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.sunriseydy.acgn.client.setLocalServerConfig
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * @author SunriseYDY
 * @date 2024-07-31 16:06
 */

private val logger = KotlinLogging.logger { }

@Composable
fun ServerConfig(unShow: () -> Unit) {
    Button(
        onClick = {
            setLocalServerConfig("http://localhost:9390")
            unShow()
        }
    ) {
        Text("test")
    }
}