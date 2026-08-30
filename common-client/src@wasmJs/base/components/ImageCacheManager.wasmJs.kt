package dev.sunriseydy.acgn.client.base.components

import androidx.compose.ui.graphics.ImageBitmap
import dev.sunriseydy.acgn.tools.PlatformLock
import org.jetbrains.compose.resources.decodeToImageBitmap

private class ImageMemoryCache(private val maxSize: Int = 20) {
    private val lock = PlatformLock()
    private val keys = ArrayDeque<String>()
    private val map = mutableMapOf<String, ImageBitmap>()

    fun get(key: String): ImageBitmap? = lock.withLock {
        if (key in map) {
            keys.remove(key)
            keys.addLast(key)
        }
        map[key]
    }

    fun put(key: String, bitmap: ImageBitmap) = lock.withLock {
        if (key in map) {
            keys.remove(key)
        } else if (keys.size >= maxSize) {
            val oldest = keys.removeFirstOrNull()
            if (oldest != null) {
                map.remove(oldest)
            }
        }
        keys.addLast(key)
        map[key] = bitmap
    }
}

internal actual fun getImageFromMemory(key: String): ImageBitmap? = WasmJsImageCacheManager.getFromMemory(key)

internal actual suspend fun loadImagePlatform(key: String, fetch: suspend () -> ByteArray): ImageBitmap? =
    WasmJsImageCacheManager.loadImage(key, fetch)

private object WasmJsImageCacheManager {
    private val memoryCache = ImageMemoryCache()

    fun getFromMemory(key: String): ImageBitmap? = memoryCache.get(key)

    suspend fun loadImage(key: String, fetch: suspend () -> ByteArray): ImageBitmap? {
        val bytes = fetch()
        val bitmap = bytes.decodeToImageBitmap()
        memoryCache.put(key, bitmap)
        return bitmap
    }
}

