package dev.sunriseydy.acgn.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.client.base.api.SyAcgnApi
import dev.sunriseydy.acgn.client.base.components.ServerConfig
import dev.sunriseydy.acgn.client.base.enums.AcgnContentType
import dev.sunriseydy.acgn.client.base.navigation.AcgnNavigationWrapper
import dev.sunriseydy.acgn.client.base.navigation.NavigationAction
import dev.sunriseydy.acgn.client.base.navigation.NavigationRoute
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope

private val logger = KotlinLogging.logger { }

@Composable
fun App() {
    MaterialTheme {
        Surface {
            val showServerConfig = remember { mutableStateOf(true) }
            if (showServerConfig.value) {
                val (success, message) = checkServer(showServerConfig)
                if (showServerConfig.value) {
                    ServerConfig(onClick = { checkServer(showServerConfig, it) }, success, message)
                } else {
                    AcgnNavigationWrapper()
                }
            } else {
                AcgnNavigationWrapper()
            }
        }
    }
}

private fun checkServer(showServerConfig: MutableState<Boolean>, language: Language? = null) =
    try {
        val (_, configs, localizations) = SyAcgnApi().common.getAppInfo(language).checkSuccessAndNotNull()
        AppConfigTool.putAll(configs)
        if (localizations.isEmpty()) {
            throw error("localization is empty")
        }
        LocalizationTool.putAll(localizations)
        showServerConfig.value = false
        Pair(true, "连接服务器成功")
    } catch (e: Exception) {
        showServerConfig.value = true
        Pair(false, "连接服务器失败：${e.message}")
    }

/**
 * @author SunriseYDY
 * @date 2026-02-13 16:05
 */
data class AppState(
    val navigationAction: NavigationAction<NavigationRoute>,
    val snackbarHostState: SnackbarHostState,
    val scope: CoroutineScope,
    val contentType: AcgnContentType,
    val api: SyAcgnApi,
)