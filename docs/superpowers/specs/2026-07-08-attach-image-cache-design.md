# AttachImage Component Image Caching Design Spec

- **Date**: 2026-07-08
- **Author**: yukino
- **Status**: Approved

## 1. Context & Motivation

Currently, the `AttachImage` Compose component in the client fetches raw image bytes from the server using `appState.api.common.getAttachFileBytes(attachId)` and decodes it to an `ImageBitmap` every time the component is recomposed or loaded. This causes:
1. Significant and redundant server load and network data transfer.
2. Visual delays (blank states or progress indicators) when navigating back and forth between pages that display the same images.
3. Lag/jank on the UI thread due to synchronous-style decoding or blocking IO.

To resolve these issues, we will implement a dual-layer caching mechanism (in-memory LRU cache and a persistent disk cache) and ensure all IO/decoding is offloaded to the IO dispatcher.

---

## 2. Requirements & Goals

* **Memory Cache**: Hold decoded `ImageBitmap` objects for immediate rendering on recomposition and list scrolling. It should have a maximum capacity (e.g., 50 images) to prevent out-of-memory errors and evict the least recently used entries.
* **Disk Cache**: Store the raw downloaded image bytes under the user's home directory (`~/.sy-acgn/cache/images`) to persist images across application restarts.
* **Thread Safety**: Both cache operations and decoding must be thread-safe and non-blocking to the main UI thread.
* **Fallback Behavior**: If caching or file operations fail, the component should gracefully fallback to downloading the image directly and display it without crashing.

---

## 3. Detailed Architecture

```
+-------------------------------------------------------------+
|                          AttachImage                        |
+-------------------------------------------------------------+
                               |
                               v
               +------------------------------+
               |      ImageCacheManager       |
               +------------------------------+
                 /                          \
                v                            v
  +--------------------------+  +--------------------------+
  |    ImageMemoryCache      |  |     ImageDiskCache       |
  |  (Holds ImageBitmap,     |  |  (Holds raw binary files |
  |   LRU eviction, max=50)  |  |   on local filesystem)   |
  +--------------------------+  +--------------------------+
```

### 3.1 Cache Classes
1. **`ImageMemoryCache`**: A thread-safe wrapper around a `LinkedHashMap` configured with `accessOrder = true` to implement LRU eviction.
2. **`ImageDiskCache`**: A helper class that checks for, reads from, and writes to a designated local cache directory.
3. **`ImageCacheManager`**: A singleton (`object`) that integrates memory and disk caches, providing unified accessors.

### 3.2 Threading
File reads/writes, network calls, and image decoding via `ImageIO.read` will be moved into a coroutine running on `Dispatchers.IO` using `withContext(Dispatchers.IO)`.

---

## 4. Implementation Details

We will implement all helper classes directly in `common-client/src/base/components/AttachImage.kt` as private/internal classes and an object, keeping the implementation simple and self-contained.

### 4.1 Memory Cache
```kotlin
private class ImageMemoryCache(private val maxSize: Int = 50) {
    private val cache = java.util.Collections.synchronizedMap(
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
    fun clear() = cache.clear()
}
```

### 4.2 Disk Cache
```kotlin
private class ImageDiskCache(private val cacheDir: java.io.File) {
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    private fun getFile(key: String): java.io.File = java.io.File(cacheDir, key)

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
            // Silently ignore or log write failures so they don't break UI loading
        }
    }
}
```

### 4.3 Cache Manager
```kotlin
internal object ImageCacheManager {
    private val cacheDir = java.io.File(System.getProperty("user.home"), ".sy-acgn/cache/images")
    private val memoryCache = ImageMemoryCache()
    private val diskCache = ImageDiskCache(cacheDir)

    fun getFromMemory(key: String): ImageBitmap? = memoryCache.get(key)

    fun getFromDiskAndCache(key: String): ImageBitmap? {
        val bytes = diskCache.get(key) ?: return null
        return try {
            val bitmap = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(bytes))?.toComposeImageBitmap()
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
```

### 4.4 Flow in `AttachImage`
```kotlin
    LaunchedEffect(attachId) {
        if (attachId.isBlank()) {
            loading.value = false
            return@LaunchedEffect
        }
        loading.value = true
        error.value = false
        try {
            // 1. Memory Check
            val cachedBitmap = ImageCacheManager.getFromMemory(attachId)
            if (cachedBitmap != null) {
                imageBitmap.value = cachedBitmap
                loading.value = false
                return@LaunchedEffect
            }

            // 2. IO & Decoding in background thread
            val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                // Check Disk
                var loaded = ImageCacheManager.getFromDiskAndCache(attachId)
                if (loaded == null) {
                    // Download
                    val bytes = appState.api.common.getAttachFileBytes(attachId)
                    loaded = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(bytes))?.toComposeImageBitmap()
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
            error.value = true
        } finally {
            loading.value = false
        }
    }
```

---

## 5. Verification Plan

1. **Compilation**: Run `./kotlin build -m common-client` to verify it compiles.
2. **Execution Check**: Verify that when images load:
   - A directory `~/.sy-acgn/cache/images` is created.
   - Files with UUID names (corresponding to `attachId`) are populated in this directory.
   - Returning to the page (e.g. `AnimeSeason`) loads the image instantly without showing the loading spinner, indicating a memory or disk cache hit.
