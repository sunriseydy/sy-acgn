package dev.sunriseydy.acgn.server.common.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.dto.AppInfo
import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository
import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.server.plugins.loadLocalizations
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * @author SunriseYDY
 * @date 2024-07-12 11:55
 */
fun Routing.configureCommonModuleRoutes() {
    route("/common") {
        get("/info") {
            call.parameters["language"]?.let {
                LocalizationTool.loadLocalizations(Language.valueOf(it))
            }
            call.respond(
                Result(
                    data = AppInfo(
                        configs = AppConfigTool.getAppConfigs(),
                        localizations = LocalizationTool.getLocalizations()
                    )
                )
            )
        }
        get("/localization") {
            call.respond(Result(data = LocalizationTool.getLocalizations()))
        }
        route("/config") {
            val appConfigService = AppConfigService()
            get {
                call.respond(Result(data = appConfigService.getAllAppConfigFromDB()))
            }
            get("/map") {
                call.respond(Result(data = AppConfigTool.getAppConfigs()))
            }
            post {
                val appConfigs = call.receive<List<AppConfig>>()
                call.respond(Result(data = appConfigService.saveAppConfigs(appConfigs)))
            }
        }
        route("/addition") {
            val additionalInfoRepository = AdditionalInfoRepository()
            get {
                val associatedType = call.parameters["associatedType"]!!
                val associatedId = call.parameters["associatedId"]!!.toULong()
                val additionalType = call.parameters["additionalType"]
                call.respond(
                    Result(
                        data = additionalInfoRepository.selectAdditionalInfos(
                            associatedType,
                            associatedId,
                            additionalType
                        )
                    )
                )
            }
            post {
                call.respond(Result(data = additionalInfoRepository.saveAdditionalInfo(call.receive())))
            }
            delete {
                call.respond(Result(data = additionalInfoRepository.deleteAdditionalInfo(call.parameters["id"]!!)))
            }
        }
    }
}