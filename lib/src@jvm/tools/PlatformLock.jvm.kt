package dev.sunriseydy.acgn.tools

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 跨平台互斥锁抽象，用于保护共享内存状态的读写（JVM 实现）。
 */
actual class PlatformLock actual constructor() {
    private val lock = ReentrantLock()

    actual fun <T> withLock(action: () -> T): T = lock.withLock(action)
}
