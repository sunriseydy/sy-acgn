package dev.sunriseydy.acgn.client.base.components

import androidx.compose.ui.graphics.ImageBitmap
import dev.sunriseydy.acgn.client.base.utils.AppDirectories
import dev.sunriseydy.acgn.tools.PlatformLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.File
import java.security.MessageDigest

private class ImageMemoryCache(private val maxSize: Int = 20) {
    private val lock = PlatformLock()
    private val cache = object : LinkedHashMap<String, ImageBitmap>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, ImageBitmap>?): Boolean {
            return size > maxSize
        }
    }

    fun get(key: String): ImageBitmap? = lock.withLock { cache[key] }
    fun put(key: String, bitmap: ImageBitmap) = lock.withLock {
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

internal actual fun getImageFromMemory(key: String): ImageBitmap? = JvmImageCacheManager.getFromMemory(key)

internal actual suspend fun loadImagePlatform(key: String, fetch: suspend () -> ByteArray): ImageBitmap? =
    JvmImageCacheManager.loadImage(key, fetch)

private object JvmImageCacheManager {
    private val cacheDir = File(AppDirectories.appCacheDir, "images")
    private val memoryCache = ImageMemoryCache()
    private val diskCache = ImageDiskCache(cacheDir)

    fun getFromMemory(key: String): ImageBitmap? = memoryCache.get(key)

    suspend fun loadImage(key: String, fetch: suspend () -> ByteArray): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            val diskBytes = diskCache.get(key)
            if (diskBytes != null) {
                try {
                    val bitmap = diskBytes.decodeToImageBitmap()
                    memoryCache.put(key, bitmap)
                    return@withContext bitmap
                } catch (e: Exception) {
                    diskCache.delete(key)
                }
            }

            val bytes = fetch()
            val bitmap = bytes.decodeToImageBitmap()
            memoryCache.put(key, bitmap)
            diskCache.put(key, bytes)
            bitmap
        }
    }
}

