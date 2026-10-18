package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.AppState
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

/**
 * 外部跳转链接 Chip 组件
 *
 * @param label 平台显示名称（如 "TMDB", "Bangumi", "Steam"）
 * @param url 跳转的目标 URL
 * @param appState 应用状态（用于发生错误时弹出提示）
 * @param modifier 修饰符
 */
@Composable
fun ExternalLinkChip(
    label: String,
    url: String,
    appState: AppState? = null,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    AssistChip(
        onClick = {
            try {
                uriHandler.openUri(url)
            } catch (e: Throwable) {
                logger.error(e) { "Failed to open url: $url" }
                appState?.showError("打开链接失败: ${e.message ?: url}")
            }
        },
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "打开 $label 条目页面",
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier
    )
}

/**
 * TMDB 条目跳转链接 Chip
 *
 * @param tmdbId TMDB TV Show ID
 * @param seasonNumber 季度编号，大于 0 时跳转到该季度页面，否则跳转到 TV Show 主页面
 */
@Composable
fun TmdbLinkChip(
    tmdbId: ULong,
    seasonNumber: Int? = null,
    appState: AppState? = null,
    modifier: Modifier = Modifier,
) {
    val url = if (seasonNumber != null && seasonNumber > 0) {
        "https://www.themoviedb.org/tv/$tmdbId/season/$seasonNumber"
    } else {
        "https://www.themoviedb.org/tv/$tmdbId"
    }
    ExternalLinkChip(
        label = "TMDB",
        url = url,
        appState = appState,
        modifier = modifier
    )
}

/**
 * Bangumi 条目跳转链接 Chip
 *
 * @param bgmId Bangumi Subject ID
 */
@Composable
fun BangumiLinkChip(
    bgmId: ULong,
    appState: AppState? = null,
    modifier: Modifier = Modifier,
) {
    ExternalLinkChip(
        label = "Bangumi",
        url = "https://bgm.tv/subject/$bgmId",
        appState = appState,
        modifier = modifier
    )
}

/**
 * Steam 条目跳转链接 Chip
 *
 * @param steamId Steam App ID
 */
@Composable
fun SteamLinkChip(
    steamId: ULong,
    appState: AppState? = null,
    modifier: Modifier = Modifier,
) {
    ExternalLinkChip(
        label = "Steam",
        url = "https://store.steampowered.com/app/$steamId",
        appState = appState,
        modifier = modifier
    )
}
