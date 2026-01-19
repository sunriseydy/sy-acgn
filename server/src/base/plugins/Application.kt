package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.*

suspend fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    AppConfigTool.loadAppConfigFromFile(environment)
    configureDatabases()
    AppConfigTool.loadAppConfigFromDB()
    configureLocalization()
    configureRouting()
}