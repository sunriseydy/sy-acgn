package dev.sunriseydy.acgn

class LinuxPlatform(
    override val name: String = "Linux"
): Platform

actual fun getPlatform(): Platform = LinuxPlatform()