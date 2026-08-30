package dev.sunriseydy.acgn.tools

/**
 * 跨平台互斥锁抽象，用于保护共享内存状态的读写（Wasm-JS 单线程实现）。
 */
private class WasmJsPlatformLock : PlatformLock {
    override fun <T> withLock(action: () -> T): T = action()
}

actual fun PlatformLock(): PlatformLock = WasmJsPlatformLock()
