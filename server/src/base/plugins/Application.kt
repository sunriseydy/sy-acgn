package dev.sunriseydy.acgn.server.base.plugins

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