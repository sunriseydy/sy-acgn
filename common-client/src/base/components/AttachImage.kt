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
import dev.sunriseydy.acgn.client.base.utils.AppDirectories
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
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
            // 1. Check Memory Cache
            val memoryBitmap = ImageCacheManager.getFromMemory(attachId)
            if (memoryBitmap != null) {
                imageBitmap.value = memoryBitmap
                loading.value = false
                return@LaunchedEffect
            }

            // 2. Load and decode asynchronously on background IO dispatcher
            val bitmap = withContext(Dispatchers.IO) {
                // Try from disk
                var loaded = ImageCacheManager.getFromDiskAndCache(attachId)
                if (loaded == null) {
                    // Download from API
                    val bytes = appState.api.common.getAttachFileBytes(attachId)
                    loaded = ImageIO.read(ByteArrayInputStream(bytes))?.toComposeImageBitmap()
                    if (loaded != null) {
                        ImageCacheManager.put(attachId, bytes, loaded)
                    }
                }
                loaded
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

private class ImageMemoryCache(private val maxSize: Int = 20) {
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
    private val lock = Any()
    private var isDirCreated = false
    private var putCount = 0

    private fun ensureDirCreated() {
        if (!isDirCreated) {
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            isDirCreated = true
        }
    }

    private fun getSafeFilename(key: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hashBytes = digest.digest(key.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback: replace any non-alphanumeric chars
            key.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        }
    }

    private fun getFile(key: String): File = File(cacheDir, getSafeFilename(key))

    fun get(key: String): ByteArray? = synchronized(lock) {
        ensureDirCreated()
        val file = getFile(key)
        if (file.exists() && file.isFile) {
            try {
                file.readBytes()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun put(key: String, bytes: ByteArray) = synchronized(lock) {
        try {
            ensureDirCreated()
            val file = getFile(key)
            file.writeBytes(bytes)
            putCount++
            if (putCount >= 50) {
                putCount = 0
                pruneCache()
            }
        } catch (e: Exception) {
            // Silently ignore disk write failures
        }
    }

    fun delete(key: String) = synchronized(lock) {
        try {
            ensureDirCreated()
            val file = getFile(key)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Ignore deletion failures
        }
    }

    private fun pruneCache() {
        try {
            val files = cacheDir.listFiles() ?: return
            if (files.size > 1000) {
                val sortedFiles = files.sortedBy { it.lastModified() }
                val toDeleteCount = files.size - 800
                for (i in 0 until toDeleteCount) {
                    sortedFiles[i].delete()
                }
            }
        } catch (e: Exception) {
            // Ignore pruning exceptions
        }
    }
}

internal object ImageCacheManager {
    private val cacheDir = File(AppDirectories.appCacheDir, "images")
    private val memoryCache = ImageMemoryCache()
    private val diskCache = ImageDiskCache(cacheDir)

    fun getFromMemory(key: String): ImageBitmap? = memoryCache.get(key)

    fun getFromDiskAndCache(key: String): ImageBitmap? {
        val bytes = diskCache.get(key) ?: return null
        return try {
            val bitmap = ImageIO.read(ByteArrayInputStream(bytes))?.toComposeImageBitmap()
            if (bitmap != null) {
                memoryCache.put(key, bitmap)
            } else {
                diskCache.delete(key) // Delete corrupt file on disk
            }
            bitmap
        } catch (e: Exception) {
            diskCache.delete(key) // Delete corrupt file on disk
            null
        }
    }

    fun put(key: String, bytes: ByteArray, bitmap: ImageBitmap) {
        memoryCache.put(key, bitmap)
        diskCache.put(key, bytes)
    }
}
