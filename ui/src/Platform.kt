package dev.sunriseydy.acgn.ui

interface Platform {
    val name: String
}

class JVMPlatform(
    override val name: String = "Java ${System.getProperty("java.version")}"
) : Platform

fun getPlatform(): Platform = JVMPlatform()