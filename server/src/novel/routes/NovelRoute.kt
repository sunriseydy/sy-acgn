package dev.sunriseydy.acgn.server.novel.routes

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.novel.NovelModuleResource
import dev.sunriseydy.acgn.novel.dto.NovelCreateOrUpdateDto
import dev.sunriseydy.acgn.novel.dto.NovelVolumeCreateOrUpdateDto
import dev.sunriseydy.acgn.server.novel.service.NovelService
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureNovelModuleRoutes() {
    val novelService: NovelService by application.dependencies

    // --- Novel CRUD ---
    get<NovelModuleResource.Novel.List> { resource ->
        call.respond(
            Result(
                data = novelService.getNovelList(
                    name = resource.name,
                    status = resource.status,
                    page = resource.page,
                    size = resource.size
                )
            )
        )
    }

    get<NovelModuleResource.Novel.Id> { resource ->
        call.respond(Result(data = novelService.getNovelById(resource.id)))
    }

    post<NovelModuleResource.Novel> {
        val dto: NovelCreateOrUpdateDto = call.receive()
        call.respond(Result(data = novelService.createNovel(dto)))
    }

    put<NovelModuleResource.Novel> {
        val dto: NovelCreateOrUpdateDto = call.receive()
        call.respond(Result(data = novelService.updateNovel(dto)))
    }

    delete<NovelModuleResource.Novel.Id> { resource ->
        novelService.deleteNovel(resource.id)
        call.respond(Result(data = true))
    }

    // --- Novel Volume ---
    post<NovelModuleResource.Novel.Volume> {
        val dto: NovelVolumeCreateOrUpdateDto = call.receive()
        call.respond(Result(data = novelService.createVolume(dto)))
    }

    put<NovelModuleResource.Novel.Volume> {
        val dto: NovelVolumeCreateOrUpdateDto = call.receive()
        call.respond(Result(data = novelService.updateVolume(dto)))
    }

    delete<NovelModuleResource.Novel.Volume.Id> { resource ->
        novelService.deleteVolume(resource.volumeId)
        call.respond(Result(data = true))
    }

    put<NovelModuleResource.Novel.Volume.ReadingStatus> { resource ->
        val status: String = call.receive<Map<String, String>>()["readingStatus"]
            ?: throw IllegalArgumentException("readingStatus is required")
        call.respond(Result(data = novelService.updateVolumeReadingStatus(resource.volumeId, status)))
    }

    // --- Bangumi ---
    get<NovelModuleResource.Novel.Bangumi.Search> { resource ->
        call.respond(Result(data = novelService.searchBangumiNovel(resource.query)))
    }

    post<NovelModuleResource.Novel.Bangumi.Import> { resource ->
        call.respond(Result(data = novelService.importNovelFromBangumi(resource.bgmId, resource.isUpdate)))
    }
}
