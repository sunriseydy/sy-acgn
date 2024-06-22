package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.Application

@Suppress("unused")
fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}