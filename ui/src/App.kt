package dev.sunriseydy.acgn.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.ui.components.ServerConfig
import dev.sunriseydy.acgn.ui.navigation.AcgnNavigationWrapper
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Composable
fun App() {
    MaterialTheme {
        Surface {
            var server by remember { mutableStateOf(AppConfigTool.getLocalServerConfig()) }
            if (server != null) {
                AcgnNavigationWrapper()
            } else {
                ServerConfig {
                    server = AppConfigTool.setLocalServerConfig(it)
                }
            }
        }
    }
}