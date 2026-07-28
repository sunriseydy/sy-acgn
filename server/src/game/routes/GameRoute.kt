package dev.sunriseydy.acgn.server.game.routes

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.game.GameModuleResource
import dev.sunriseydy.acgn.game.dto.GameCreateOrUpdateDto
import dev.sunriseydy.acgn.game.dto.GamePlayRecordCreateOrUpdateDto
import dev.sunriseydy.acgn.game.dto.GameReleaseCreateOrUpdateDto
import dev.sunriseydy.acgn.server.game.service.GameService
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureGameModuleRoutes() {
    val gameService: GameService by application.dependencies

    // --- Game CRUD ---
    get<GameModuleResource.Game.List> { resource ->
        call.respond(
            Result(
                data = gameService.getGameList(
                    name = resource.name,
                    platform = resource.platform,
                    playStatus = resource.playStatus,
                    page = resource.page,
                    size = resource.size
                )
            )
        )
    }

    get<GameModuleResource.Game.Id> { resource ->
        call.respond(Result(data = gameService.getGameById(resource.id)))
    }

    post<GameModuleResource.Game> {
        val dto: GameCreateOrUpdateDto = call.receive()
        call.respond(Result(data = gameService.createGame(dto)))
    }

    put<GameModuleResource.Game> {
        val dto: GameCreateOrUpdateDto = call.receive()
        call.respond(Result(data = gameService.updateGame(dto)))
    }

    delete<GameModuleResource.Game.Id> { resource ->
        gameService.deleteGame(resource.id)
        call.respond(Result(data = true))
    }

    // --- Play Record ---
    get<GameModuleResource.Game.PlayRecord> { resource ->
        call.respond(Result(data = gameService.getPlayRecord(resource.id)))
    }

    post<GameModuleResource.Game.PlayRecord> {
        val dto: GamePlayRecordCreateOrUpdateDto = call.receive()
        call.respond(Result(data = gameService.updatePlayRecord(dto)))
    }

    // --- Releases ---
    get<GameModuleResource.Game.Release> { resource ->
        call.respond(Result(data = gameService.getReleasesByGameId(resource.id)))
    }

    post<GameModuleResource.Game.Release> {
        val dto: GameReleaseCreateOrUpdateDto = call.receive()
        call.respond(Result(data = gameService.createRelease(dto)))
    }

    put<GameModuleResource.Game.Release> {
        val dto: GameReleaseCreateOrUpdateDto = call.receive()
        call.respond(Result(data = gameService.updateRelease(dto)))
    }

    delete<GameModuleResource.Game.Release.Id> { resource ->
        gameService.deleteRelease(resource.releaseId)
        call.respond(Result(data = true))
    }

    // --- Bangumi ---
    get<GameModuleResource.Game.Bangumi.Search> { resource ->
        call.respond(Result(data = gameService.searchBangumiGame(resource.query)))
    }

    post<GameModuleResource.Game.Bangumi.Import> { resource ->
        call.respond(Result(data = gameService.importFromBangumi(resource.bgmId, resource.isUpdate)))
    }

    // --- Steam ---
    get<GameModuleResource.Game.Steam.Search> { resource ->
        call.respond(Result(data = gameService.searchSteamGame(resource.query)))
    }

    post<GameModuleResource.Game.Steam.Import> { resource ->
        call.respond(Result(data = gameService.importFromSteam(resource.appId, resource.isUpdate)))
    }
}
