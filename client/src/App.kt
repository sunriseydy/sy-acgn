package dev.sunriseydy.acgn.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.sunriseydy.acgn.client.components.ServerConfig
import dev.sunriseydy.acgn.client.navigation.AcgnNavigationWrapper
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

private val logger = KotlinLogging.logger { }

@Composable
fun App() {
    MaterialTheme {
        Surface {
            var showServerConfig by remember { mutableStateOf(false) }
            val server = getLocalServerConfig()
            if (server == null) {
                showServerConfig = true
            } else {
                runBlocking {
                    try {
                        val (version, configs, localizations) = SyAcgnApi().common.getAppInfo().checkSuccessAndNotNull()
                        AppConfigTool.putAll(configs)
                        LocalizationTool.putAll(localizations)
                    } catch (e: Exception) {
                        logger.error(e) { "error: ${e.message}" }
                        showServerConfig = true
                    }
                }
            }
            if (showServerConfig) {
                ServerConfig {
                    showServerConfig = false
                }
            } else {
                AcgnNavigationWrapper()
            }
        }
    }
}