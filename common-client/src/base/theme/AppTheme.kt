package dev.sunriseydy.acgn.client.base.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import dev.sunriseydy.acgn.client.res.Res
import dev.sunriseydy.acgn.client.res.mi_sans_normal
import org.jetbrains.compose.resources.Font

/**
 * 构建使用 Mi Sans 中文字体的自定义 Typography
 *
 * 在 wasmJs 平台上，Skiko Canvas 渲染不使用 HTML/CSS 字体，
 * 必须通过 Compose Resources 显式加载中文字体才能正确显示中文字符。
 *
 * @author SunriseYDY
 */
@Composable
fun appTypography(): Typography {
    val fontFamily = FontFamily(
        Font(Res.font.mi_sans_normal),
    )
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily),
    )
}

/**
 * 应用主题包装组件
 *
 * 使用自定义中文字体 Typography 的 MaterialTheme。
 * 确保所有子组件都能正确显示中文字符。
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = appTypography(),
        content = content,
    )
}
