package dev.sunriseydy.acgn.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Composable
fun App() {
    MaterialTheme {
        Surface {
            AppConfigTool.clearLocalServerConfig()
            val (server, setServer) = remember { mutableStateOf(AppConfigTool.getLocalServerConfig()) }
//            logger.info { "server: $server" }
            if (server == null) {
                setServer(AppConfigTool.setLocalServerConfig("test"))
            }
//            logger.info { "server: $server" }
            if (server != null) {
                Text(
                    text = "Hello, $server!",
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedTextField(
                value = server ?: "",
                onValueChange = { setServer(it) },
                label = { Text("Name") }
            )
            Surface {
                logger.info { "server: $server" }
            }
//            AcgnNavigationWrapper()
        }
    }
}