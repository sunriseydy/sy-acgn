package dev.sunriseydy.acgn

class JVMPlatform(
    override val name: String = "Java ${System.getProperty("java.version")}"
): Platform

actual fun getPlatform(): Platform = JVMPlatform()