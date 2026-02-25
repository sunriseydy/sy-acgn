package dev.sunriseydy.acgn.tools

internal actual class PlatformLock actual constructor() {
    actual fun <T> withLock(action: () -> T): T = action()
}
