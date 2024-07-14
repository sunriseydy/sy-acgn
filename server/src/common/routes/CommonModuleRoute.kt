package dev.sunriseydy.acgn.common.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.service.AppConfigService
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * @author SunriseYDY
 * @date 2024-07-12 11:55
 */
fun Route.configureCommonModuleRoutes() {
    route("/common") {
        get("/localizations") {
            call.respond(Result(data = LocalizationTool.localizations))
        }
        route("/config") {
            val appConfigService = AppConfigService()
            get("/") {
                call.respond(Result(data = appConfigService.getAllAppConfigFromDB()))
            }
            get("/map") {
                call.respond(Result(data = AppConfigTool.appConfig))
            }
            post {
                val appConfigs = call.receive<List<AppConfig>>()
                call.respond(Result(data = appConfigService.saveAppConfigs(appConfigs)))
            }
        }
    }
}