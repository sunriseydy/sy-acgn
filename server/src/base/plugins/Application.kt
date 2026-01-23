package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

suspend fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureDependencyInjection()
    AppConfigTool.loadAppConfigFromFile(environment)
    configureDatabases()
    AppConfigTool.loadAppConfigFromDB(dependencies.resolve<AppConfigService>())
    configureLocalization()
    configureRouting()
}