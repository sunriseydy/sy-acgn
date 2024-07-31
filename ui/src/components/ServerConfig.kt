package dev.sunriseydy.acgn.ui.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2024-07-31 16:06
 */
@Composable
fun ServerConfig(setServer: (String) -> Unit) {
    Button(
        onClick = {
            setServer(AppConfigTool.setLocalServerConfig("test"))
            LocalizationTool.putLocalization("enum.COMMON.AcgnNavigationRoute.RSS", "订阅")
        }
    ) {
        Text("test")
    }
}