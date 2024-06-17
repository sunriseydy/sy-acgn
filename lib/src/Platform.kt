package dev.sunriseydy.acgn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform