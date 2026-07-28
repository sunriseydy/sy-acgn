package dev.sunriseydy.acgn.client.game.api

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.game.GameModuleResource
import dev.sunriseydy.acgn.game.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*

/**
 * 游戏 API 客户端
 */
class GameApi internal constructor(private val httpClient: HttpClient) {
    suspend fun getGameList(
        name: String? = null,
        platform: String? = null,
        playStatus: String? = null,
        page: Long = 1,
        size: Int = 50
    ): Result<List<Game>> =
        httpClient.get(GameModuleResource.Game.List(name = name, platform = platform, playStatus = playStatus, page = page, size = size)).body()

    suspend fun getGameById(id: ULong): Result<Game> =
        httpClient.get(GameModuleResource.Game.Id(id = id)).body()

    suspend fun createGame(dto: GameCreateOrUpdateDto): Result<Game> =
        httpClient.post(GameModuleResource.Game()) { setBody(dto) }.body()

    suspend fun updateGame(dto: GameCreateOrUpdateDto): Result<Game> =
        httpClient.put(GameModuleResource.Game()) { setBody(dto) }.body()

    suspend fun deleteGame(id: ULong): Result<Boolean> =
        httpClient.delete(GameModuleResource.Game.Id(id = id)).body()

    suspend fun getPlayRecord(gameId: ULong): Result<GamePlayRecord?> =
        httpClient.get(GameModuleResource.Game.PlayRecord(id = gameId)).body()

    suspend fun updatePlayRecord(dto: GamePlayRecordCreateOrUpdateDto): Result<GamePlayRecord> =
        httpClient.post(GameModuleResource.Game.PlayRecord(id = dto.gameId)) { setBody(dto) }.body()

    suspend fun createRelease(dto: GameReleaseCreateOrUpdateDto): Result<GameRelease> =
        httpClient.post(GameModuleResource.Game.Release(id = dto.gameId)) { setBody(dto) }.body()

    suspend fun updateRelease(dto: GameReleaseCreateOrUpdateDto): Result<GameRelease> =
        httpClient.put(GameModuleResource.Game.Release(id = dto.gameId)) { setBody(dto) }.body()

    suspend fun deleteRelease(gameId: ULong, releaseId: ULong): Result<Boolean> =
        httpClient.delete(GameModuleResource.Game.Release.Id(parent = GameModuleResource.Game.Release(id = gameId), releaseId = releaseId)).body()

    suspend fun searchBangumiGame(query: String): Result<List<Game>> =
        httpClient.get(GameModuleResource.Game.Bangumi.Search(query = query)).body()

    suspend fun importFromBangumi(bgmId: ULong, isUpdate: Boolean = false): Result<Game> =
        httpClient.post(GameModuleResource.Game.Bangumi.Import(bgmId = bgmId, isUpdate = isUpdate)).body()

    suspend fun searchSteamGame(query: String): Result<List<Game>> =
        httpClient.get(GameModuleResource.Game.Steam.Search(query = query)).body()

    suspend fun importFromSteam(appId: ULong, isUpdate: Boolean = false): Result<Game> =
        httpClient.post(GameModuleResource.Game.Steam.Import(appId = appId, isUpdate = isUpdate)).body()
}
