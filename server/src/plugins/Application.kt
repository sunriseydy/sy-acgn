package dev.sunriseydy.acgn.server.plugins

import io.ktor.server.application.*

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureDatabases()
    loadAppConfig()
    configureLocalization()
    configureRouting()
}