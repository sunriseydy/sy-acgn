package dev.sunriseydy.acgn.server.common.routes

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.common.CommonModuleResource
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.dto.AppInfo
import dev.sunriseydy.acgn.server.base.plugins.loadAppConfigFromDB
import dev.sunriseydy.acgn.server.base.plugins.loadLocalizations
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository
import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.server.common.service.AttachFileInfoService
import dev.sunriseydy.acgn.tools.AppConfigTool
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.ktor.http.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * @author SunriseYDY
 * @date 2024-07-12 11:55
 */
fun Route.configureCommonModuleRoutes() {
    val appConfigService: AppConfigService by application.dependencies
    val additionalInfoRepository: AdditionalInfoRepository by application.dependencies
    val attachFileInfoService: AttachFileInfoService by application.dependencies
    get<CommonModuleResource.Info> { resource ->
        resource.language?.let {
            LocalizationTool.loadLocalizations(it)
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
    get<CommonModuleResource.Localization> {
        call.respond(Result(data = LocalizationTool.getLocalizations()))
    }
    get<CommonModuleResource.Config> {
        call.respond(Result(data = appConfigService.getAllAppConfigFromDB()))
    }
    get<CommonModuleResource.Config.Map> {
        call.respond(Result(data = AppConfigTool.getAppConfigs()))
    }
    post<CommonModuleResource.Config> {
        val appConfigs = call.receive<List<AppConfig>>()
        call.respond(Result(data = appConfigService.saveAppConfigs(appConfigs).also { AppConfigTool.loadAppConfigFromDB(appConfigService) }))
    }
    get<CommonModuleResource.Addition> { resource ->
        val associatedType = resource.associatedType!!
        val associatedId = resource.associatedId!!
        val additionalType = resource.additionalType
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
    post<CommonModuleResource.Addition> {
        call.respond(Result(data = additionalInfoRepository.saveAdditionalInfo(call.receive())))
    }
    delete<CommonModuleResource.Addition> { resource ->
        call.respond(Result(data = additionalInfoRepository.deleteAdditionalInfo(resource.id!!)))
    }
    get<CommonModuleResource.AttachFile> { resource ->
        val fileInfo = attachFileInfoService.getFileById(resource.id)
        if (fileInfo == null) {
            call.respond(HttpStatusCode.NotFound, "File not found: ${resource.id}")
            return@get
        }
        val stream = attachFileInfoService.getFileStream(resource.id)
        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Inline
                .withParameter(ContentDisposition.Parameters.FileName, fileInfo.fileName)
                .toString()
        )
        call.respondOutputStream(ContentType.parse(fileInfo.contentType)) {
            stream.copyTo(this)
        }
    }
}