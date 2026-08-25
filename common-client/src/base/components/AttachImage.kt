package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import dev.sunriseydy.acgn.client.AppState

/**
 * 附件图片展示组件
 *
 * @param appState 应用状态
 * @param attachId 附件 ID
 * @param modifier 修饰符
 * @param contentScale 缩放策略
 * @param contentDescription 图片描述
 */
@Composable
fun AttachImage(
    appState: AppState,
    attachId: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null
) {
    val imageBitmap = remember(attachId) { mutableStateOf<ImageBitmap?>(null) }
    val loading = remember(attachId) { mutableStateOf(true) }
    val error = remember(attachId) { mutableStateOf(false) }

    LaunchedEffect(attachId) {
        if (attachId.isBlank()) {
            loading.value = false
            return@LaunchedEffect
        }
        loading.value = true
        error.value = false
        try {
            // 1. Check Memory Cache
            val memoryBitmap = ImageCacheManager.getFromMemory(attachId)
            if (memoryBitmap != null) {
                imageBitmap.value = memoryBitmap
                loading.value = false
                return@LaunchedEffect
            }

            // 2. Load from cache or API
            val bitmap = ImageCacheManager.loadImage(attachId) {
                appState.api.common.getAttachFileBytes(attachId)
            }

            if (bitmap != null) {
                imageBitmap.value = bitmap
            } else {
                error.value = true
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            error.value = true
        } finally {
            loading.value = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (loading.value) {
            CircularProgressIndicator()
        } else if (error.value || imageBitmap.value == null) {
            Text("No Image")
        } else {
            Image(
                bitmap = imageBitmap.value!!,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}
