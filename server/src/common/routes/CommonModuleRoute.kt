package dev.sunriseydy.acgn.common.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
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
    }
}