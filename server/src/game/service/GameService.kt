package dev.sunriseydy.acgn.server.game.service

import dev.sunriseydy.acgn.game.dto.*

/**
 * 游戏模块 Service 接口
 */
interface GameService {
    suspend fun getGameList(name: String? = null, platform: String? = null, playStatus: String? = null, page: Long = 1, size: Int = 50): List<Game>
    suspend fun getGameById(id: ULong): Game
    suspend fun createGame(dto: GameCreateOrUpdateDto): Game
    suspend fun updateGame(dto: GameCreateOrUpdateDto): Game
    suspend fun deleteGame(id: ULong)

    suspend fun getReleasesByGameId(gameId: ULong): List<GameRelease>
    suspend fun createRelease(dto: GameReleaseCreateOrUpdateDto): GameRelease
    suspend fun updateRelease(dto: GameReleaseCreateOrUpdateDto): GameRelease
    suspend fun deleteRelease(releaseId: ULong)

    suspend fun getPlayRecord(gameId: ULong): GamePlayRecord?
    suspend fun updatePlayRecord(dto: GamePlayRecordCreateOrUpdateDto): GamePlayRecord

    suspend fun searchBangumiGame(query: String): List<Game>
    suspend fun importFromBangumi(bgmId: ULong, isUpdate: Boolean = false): Game
    suspend fun searchSteamGame(query: String): List<Game>
    suspend fun importFromSteam(appId: ULong, isUpdate: Boolean = false): Game
}
