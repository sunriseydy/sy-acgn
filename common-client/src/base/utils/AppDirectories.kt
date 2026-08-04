package dev.sunriseydy.acgn.client.base.utils

import java.io.File

/**
 * @author SunriseYDY
 * Standard application directories helper for app data and cache across OS platforms.
 */
object AppDirectories {
    private const val APP_NAME = "sy-acgn"

    private val osName: String by lazy {
        System.getProperty("os.name")?.lowercase() ?: ""
    }

    val isWindows: Boolean by lazy { osName.contains("win") }
    val isMac: Boolean by lazy { osName.contains("mac") }

    /**
     * Standard Application Data directory:
     * - Windows: %APPDATA%/sy-acgn (fallback: %LOCALAPPDATA%/sy-acgn)
     * - macOS: ~/Library/Application Support/sy-acgn
     * - Linux/Unix: $XDG_DATA_HOME/sy-acgn (default: ~/.local/share/sy-acgn)
     */
    val appDataDir: File by lazy {
        val baseDir = when {
            isWindows -> System.getenv("APPDATA")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("LOCALAPPDATA")
                ?.takeIf { it.isNotBlank() }
                ?: System.getProperty("user.home")
            isMac -> System.getProperty("user.home")?.let { "$it/Library/Application Support" }
            else -> System.getenv("XDG_DATA_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: System.getProperty("user.home")?.let { "$it/.local/share" }
        } ?: "."
        File(baseDir, APP_NAME).apply { mkdirs() }
    }

    /**
     * Standard Application Cache directory:
     * - Windows: %LOCALAPPDATA%/sy-acgn/cache (fallback: %APPDATA%/sy-acgn/cache)
     * - macOS: ~/Library/Caches/sy-acgn
     * - Linux/Unix: $XDG_CACHE_HOME/sy-acgn (default: ~/.cache/sy-acgn)
     */
    val appCacheDir: File by lazy {
        val baseDir = when {
            isWindows -> System.getenv("LOCALAPPDATA")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("APPDATA")
                ?.takeIf { it.isNotBlank() }
                ?: System.getProperty("user.home")
            isMac -> System.getProperty("user.home")?.let { "$it/Library/Caches" }
            else -> System.getenv("XDG_CACHE_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: System.getProperty("user.home")?.let { "$it/.cache" }
        } ?: "."
        val cacheBase = if (isWindows) File(baseDir, "$APP_NAME/cache") else File(baseDir, APP_NAME)
        cacheBase.apply { mkdirs() }
    }
}
