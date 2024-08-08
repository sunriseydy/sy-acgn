package dev.sunriseydy.acgn.ui.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.sunriseydy.acgn.tools.LocalizationTool
import dev.sunriseydy.acgn.ui.setLocalServerConfig

/**
 * @author SunriseYDY
 * @date 2024-07-31 16:06
 */
@Composable
fun ServerConfig(setServer: (String) -> Unit) {
    Button(
        onClick = {
            setServer(setLocalServerConfig("test"))
            LocalizationTool.putLocalization("enum.COMMON.AcgnNavigationRoute.RSS", "订阅")
        }
    ) {
        Text("test")
    }
}