# AttachImage Caching Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add double-layered image cache (in-memory and disk) to `AttachImage.kt` to improve load time and save network resources.

**Architecture:** Implement helper classes `ImageMemoryCache` (LRU cache using `LinkedHashMap`), `ImageDiskCache` (file storage in user folder), and a singleton manager `ImageCacheManager` inside `AttachImage.kt`. Use `Dispatchers.IO` for async IO and image decoding operations.

**Tech Stack:** Kotlin, Compose Multiplatform, Java standard `File` & `ImageIO`, Kotlinx Coroutines.

## Global Constraints

* Target platform: JVM
* Save cache path: `System.getProperty("user.home") + "/.sy-acgn/cache/images"`
* Memory Cache capacity: 50 images

---

### Task 1: Add Cache Classes & Imports to AttachImage.kt

**Files:**
- Modify: `common-client/src/base/components/AttachImage.kt`

**Interfaces:**
- Consumes: Existing imports and types from `AttachImage.kt`
- Produces: `ImageCacheManager` singleton object

- [ ] **Step 1: Write the Cache Classes and Imports in AttachImage.kt**

Modify `common-client/src/base/components/AttachImage.kt` to add imports and the caching helper classes. Add these imports at the top of the file:
```kotlin
import java.io.File
import java.util.Collections
import java.util.LinkedHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```
And add these helper classes at the bottom of the file (after `AttachImage` function):
```kotlin
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
```

- [ ] **Step 2: Run verification command to ensure it compiles**

Run: `./kotlin build -m common-client`
Expected output: Build successful

- [ ] **Step 3: Commit**

```bash
git add common-client/src/base/components/AttachImage.kt
git commit -m "feat: implement memory and disk caches for AttachImage"
```

---

### Task 2: Integrate Cache Loading into AttachImage Composable

**Files:**
- Modify: `common-client/src/base/components/AttachImage.kt`

**Interfaces:**
- Consumes: `ImageCacheManager` singleton object
- Produces: Updated `AttachImage` composable with async image caching flow

- [ ] **Step 1: Modify LaunchedEffect in AttachImage to use the Cache**

In `common-client/src/base/components/AttachImage.kt`, replace the `LaunchedEffect(attachId)` implementation block (lines 42-62) with the following caching and threading code:
```kotlin
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
            error.value = true
        } finally {
            loading.value = false
        }
    }
```

- [ ] **Step 2: Run verification build**

Run: `./kotlin build -m common-client`
Expected output: Build successful

- [ ] **Step 3: Run the client and perform verification checks**

Run: `./kotlin run -m desktop-client`
Manually verify:
1. Desktop application launches and loads anime posters.
2. Check that the directory `~/.sy-acgn/cache/images` is created on your machine and files corresponding to image IDs are saved inside.
3. Switch pages and return, verifying that images reload instantly without showing the loading spinner again.

- [ ] **Step 4: Commit**

```bash
git add common-client/src/base/components/AttachImage.kt
git commit -m "feat: integrate image caching into AttachImage composable with background thread decoding"
```
