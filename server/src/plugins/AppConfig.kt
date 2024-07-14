package dev.sunriseydy.acgn.plugins

import dev.sunriseydy.acgn.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.application.Application

/**
 * @author SunriseYDY
 * @date 2024-07-14 20:32
 */
suspend fun Application.loadAppConfig() {
    AppConfigTool.loadAppConfig()
}

suspend fun AppConfigTool.loadAppConfig() = AppConfigService().loadAppConfig()