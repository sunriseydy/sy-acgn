package dev.sunriseydy.acgn.tools

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 跨平台互斥锁抽象，用于保护共享内存状态的读写（JVM 实现）。
 */
private class JvmPlatformLock : PlatformLock {
    private val lock = ReentrantLock()

    override fun <T> withLock(action: () -> T): T = lock.withLock(action)
}

actual fun PlatformLock(): PlatformLock = JvmPlatformLock()
