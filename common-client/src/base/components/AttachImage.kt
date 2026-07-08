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
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import dev.sunriseydy.acgn.client.AppState
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Collections
import java.util.LinkedHashMap
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            val bytes = appState.api.common.getAttachFileBytes(attachId)
            val bitmap = ImageIO.read(ByteArrayInputStream(bytes))?.toComposeImageBitmap()
            if (bitmap != null) {
                imageBitmap.value = bitmap
            } else {
                error.value = true
            }
        } catch (e: Exception) {
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

private class ImageMemoryCache(private val maxSize: Int = 50) {
    private val cache = Collections.synchronizedMap(
        object : LinkedHashMap<String, ImageBitmap>(maxSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, ImageBitmap>?): Boolean {
                return size > maxSize
            }
        }
    )

    fun get(key: String): ImageBitmap? = cache[key]
    fun put(key: String, bitmap: ImageBitmap) {
        cache[key] = bitmap
    }
}

private class ImageDiskCache(private val cacheDir: File) {
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    private fun getFile(key: String): File = File(cacheDir, key)

    fun get(key: String): ByteArray? {
        val file = getFile(key)
        return if (file.exists() && file.isFile) {
            try {
                file.readBytes()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun put(key: String, bytes: ByteArray) {
        try {
            val file = getFile(key)
            file.writeBytes(bytes)
        } catch (e: Exception) {
            // Silently ignore disk write failures
        }
    }
}

internal object ImageCacheManager {
    private val cacheDir = File(System.getProperty("user.home"), ".sy-acgn/cache/images")
    private val memoryCache = ImageMemoryCache()
    private val diskCache = ImageDiskCache(cacheDir)

    fun getFromMemory(key: String): ImageBitmap? = memoryCache.get(key)

    fun getFromDiskAndCache(key: String): ImageBitmap? {
        val bytes = diskCache.get(key) ?: return null
        return try {
            val bitmap = ImageIO.read(ByteArrayInputStream(bytes))?.toComposeImageBitmap()
            if (bitmap != null) {
                memoryCache.put(key, bitmap)
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun put(key: String, bytes: ByteArray, bitmap: ImageBitmap) {
        memoryCache.put(key, bitmap)
        diskCache.put(key, bytes)
    }
}
