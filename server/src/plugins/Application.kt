package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.*

@Suppress("unused")
fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
    configureDatabases()
    initializeDatabase()
}