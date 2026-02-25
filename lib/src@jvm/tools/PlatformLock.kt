package dev.sunriseydy.acgn.tools

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal actual class PlatformLock actual constructor() {
    private val lock = ReentrantLock()

    actual fun <T> withLock(action: () -> T): T = lock.withLock(action)
}
