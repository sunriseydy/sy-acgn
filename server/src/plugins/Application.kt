package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking

@Suppress("unused")
fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureDatabases()
    loadAppConfig()
    configureLocalization()
    configureRouting()
}