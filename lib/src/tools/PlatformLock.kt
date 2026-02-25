package dev.sunriseydy.acgn.tools

/**
 * 跨平台互斥锁抽象，用于保护共享内存状态的读写。
 */
internal expect class PlatformLock() {
    fun <T> withLock(action: () -> T): T
}
