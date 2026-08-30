package dev.sunriseydy.acgn.client.base.components

import androidx.compose.ui.graphics.ImageBitmap

internal object ImageCacheManager {
    fun getFromMemory(key: String): ImageBitmap? = getImageFromMemory(key)
    suspend fun loadImage(key: String, fetch: suspend () -> ByteArray): ImageBitmap? = loadImagePlatform(key, fetch)
}

internal expect fun getImageFromMemory(key: String): ImageBitmap?
internal expect suspend fun loadImagePlatform(key: String, fetch: suspend () -> ByteArray): ImageBitmap?
