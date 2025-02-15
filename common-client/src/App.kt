package dev.sunriseydy.acgn.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.ServerConfig
import dev.sunriseydy.acgn.client.components.showError
import dev.sunriseydy.acgn.client.navigation.AcgnNavigationAction
import dev.sunriseydy.acgn.client.navigation.AcgnNavigationWrapper
import dev.sunriseydy.acgn.client.utils.AcgnContentType
import dev.sunriseydy.acgn.enums.Language
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

enum class LayoutType {
    HEADER, CONTENT
}

data class AppState(
    val navController: NavHostController,
    val navigationAction: AcgnNavigationAction,
    val snackbarHostState: SnackbarHostState,
    val scope: CoroutineScope,
    val contentType: AcgnContentType,
    val api: SyAcgnApi,
)

fun <T> Result<T>.onSuccess(
    appState: AppState? = null,
    onSuccess: () -> Unit = { },
    onError: (String) -> Unit = { }
) {
    try {
        this.checkSuccess()
        onSuccess()
    } catch (e: Exception) {
        val message = e.message ?: CommonString.API_ERROR.localization
        appState?.showError(message)
        onError(message)
    }
}

fun <T> Result<T>.onSuccessData(
    appState: AppState? = null,
    onSuccess: (T) -> Unit = { },
    onError: (String) -> Unit = { throw error(it) }
) {
    try {
        onSuccess(this.checkSuccessAndNotNull())
    } catch (e: Exception) {
        val message = "${CommonString.API_ERROR.localization}: ${e.message ?: ""}"
        appState?.showError(message)
        onError(message)
    }
}