package dev.sunriseydy.acgn.client.base.components

import androidx.compose.ui.graphics.ImageBitmap

internal expect object ImageCacheManager {
    fun getFromMemory(key: String): ImageBitmap?
    suspend fun loadImage(key: String, fetch: suspend () -> ByteArray): ImageBitmap?
}
