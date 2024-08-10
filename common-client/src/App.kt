package dev.sunriseydy.acgn.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
            val showServerConfig = remember { mutableStateOf(true) }
            if (showServerConfig.value) {
                checkServer(showServerConfig)
                ServerConfig(onClick = { checkServer(showServerConfig) })
            } else {
                AcgnNavigationWrapper()
            }
        }
    }
}

private fun checkServer(showServerConfig: MutableState<Boolean>) =
    runBlocking {
        try {
            val (_, configs, localizations) = SyAcgnApi().common.getAppInfo().checkSuccessAndNotNull()
            AppConfigTool.putAll(configs)
            LocalizationTool.putAll(localizations)
            showServerConfig.component2()(false)
            return@runBlocking Pair(true, "连接服务器成功")
        } catch (e: Exception) {
            showServerConfig.component2()(true)
            return@runBlocking Pair(false, "连接服务器失败：${e.message}")
        }
    }