package dev.sunriseydy.acgn.server.game.repository

import dev.sunriseydy.acgn.game.dto.Game
import dev.sunriseydy.acgn.game.dto.GamePlayRecord
import dev.sunriseydy.acgn.game.dto.GameRelease

/**
 * 游戏模块 Repository 接口
 */
interface GameRepository {
    suspend fun selectAllGame(
        name: String? = null,
        platform: String? = null,
        playStatus: String? = null,
        page: Long = 1,
        size: Int = 50
    ): List<Game>

    suspend fun selectGameById(id: ULong): Game
    suspend fun selectGameByBgmId(bgmId: ULong): Game?
    suspend fun selectGameBySteamId(steamId: ULong): Game?
    suspend fun insertGame(game: Game): Game
    suspend fun updateGame(game: Game): Game
    suspend fun deleteGameById(id: ULong)

    suspend fun selectGameReleasesByGameId(gameId: ULong): List<GameRelease>
    suspend fun insertGameRelease(release: GameRelease): GameRelease
    suspend fun updateGameRelease(release: GameRelease): GameRelease
    suspend fun deleteGameReleaseById(id: ULong)

    suspend fun selectGamePlayRecordByGameId(gameId: ULong): GamePlayRecord?
    suspend fun upsertGamePlayRecord(record: GamePlayRecord): GamePlayRecord
}
