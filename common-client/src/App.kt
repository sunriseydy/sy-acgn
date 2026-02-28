package dev.sunriseydy.acgn.client

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.client.base.api.SyAcgnApi
import dev.sunriseydy.acgn.client.base.components.ServerConfig
import dev.sunriseydy.acgn.client.base.enums.AcgnContentType
import dev.sunriseydy.acgn.client.base.navigation.AcgnNavigationWrapper
import dev.sunriseydy.acgn.client.base.navigation.NavigationAction
import dev.sunriseydy.acgn.client.base.navigation.NavigationRoute
import dev.sunriseydy.acgn.client.base.theme.AppTheme
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope

private val logger = KotlinLogging.logger { }

/**
 * 主应用组件
 *
 * 负责应用的主题设置、服务器连接检查以及根据状态显示服务器配置界面或主导航界面。
 */
@Composable
fun App() {
    AppTheme {
        Surface {
            val showServerConfig = remember { mutableStateOf(true) }
            val checkResult = remember { mutableStateOf(Pair(false, "")) }

            LaunchedEffect(Unit) {
                checkResult.value = checkServer(showServerConfig)
            }

            if (showServerConfig.value) {
                ServerConfig(
                    onClick = { language ->
                        checkServer(showServerConfig, language)
                    },
                    checkResult.value.first,
                    checkResult.value.second
                )
            } else {
                AcgnNavigationWrapper()
            }
        }
    }
}

/**
 * 检查服务器连接
 *
 * 尝试连接服务器获取应用信息。如果连接成功，加载应用配置和本地化信息。
 * 如果连接失败或本地化信息为空，则返回错误信息。
 *
 * @param showServerConfig 控制是否显示服务器配置界面的状态
 * @param language 可选的语言参数
 * @return Pair(是否成功, 消息)
 */
private suspend fun checkServer(showServerConfig: MutableState<Boolean>, language: Language? = null) =
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
 * 应用全局状态
 *
 * 保存应用的全局状态，包括导航控制器、Snackbar 状态、协程作用域、内容类型（自适应布局用）和 API 客户端。
 *
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