package dev.sunriseydy.acgn.server.plugins

import io.ktor.server.application.*

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