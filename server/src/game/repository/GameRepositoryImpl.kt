package dev.sunriseydy.acgn.server.game.repository

import dev.sunriseydy.acgn.game.dto.Game
import dev.sunriseydy.acgn.game.dto.GamePlayRecord
import dev.sunriseydy.acgn.game.dto.GameRelease
import dev.sunriseydy.acgn.server.base.plugins.paging
import dev.sunriseydy.acgn.server.game.db.*
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.time.Instant as JavaInstant

class GameRepositoryImpl : GameRepository {

    override suspend fun selectAllGame(
        name: String?,
        platform: String?,
        playStatus: String?,
        page: Long,
        size: Int
    ): List<Game> = suspendTransaction {
        var query: Op<Boolean> = Op.TRUE

        if (!name.isNullOrBlank()) {
            query = query and ((GameTable.name like "%$name%") or (GameTable.originalName like "%$name%"))
        }

        if (!platform.isNullOrBlank()) {
            val matchingGameIds = GameReleaseDAO.find { GameReleaseTable.platform eq platform }
                .map { it.gameId }
            query = query and (GameTable.id inList matchingGameIds)
        }

        if (!playStatus.isNullOrBlank()) {
            val matchingGameIds = GamePlayRecordDAO.find { GamePlayRecordTable.playStatus eq playStatus }
                .map { it.gameId }
            query = query and (GameTable.id inList matchingGameIds)
        }

        GameDAO.find { query }
            .orderBy(GameTable.createdAt to SortOrder.DESC)
            .paging(page, size)
            .map { it.toDTO() }
    }

    override suspend fun selectGameById(id: ULong): Game = suspendTransaction {
        GameDAO.findById(id)?.toDTO() ?: throw NoSuchElementException("Game not found with id: $id")
    }

    override suspend fun selectGameByBgmId(bgmId: ULong): Game? = suspendTransaction {
        GameDAO.find { GameTable.bgmId eq bgmId }.firstOrNull()?.toDTO()
    }

    override suspend fun selectGameBySteamId(steamId: ULong): Game? = suspendTransaction {
        GameDAO.find { GameTable.steamId eq steamId }.firstOrNull()?.toDTO()
    }

    override suspend fun insertGame(game: Game): Game = suspendTransaction {
        GameDAO.new {
            name = game.name
            originalName = game.originalName
            developer = game.developer
            publisher = game.publisher
            description = game.description
            releaseDate = game.releaseDate
            bgmId = game.bgmId
            steamId = game.steamId
            rating = game.rating
        }.toDTO()
    }

    override suspend fun updateGame(game: Game): Game = suspendTransaction {
        GameDAO.findByIdAndUpdate(game.id) {
            it.name = game.name
            it.originalName = game.originalName
            it.developer = game.developer
            it.publisher = game.publisher
            it.description = game.description
            it.releaseDate = game.releaseDate
            game.bgmId?.apply { it.bgmId = game.bgmId }
            game.steamId?.apply { it.steamId = game.steamId }
            it.rating = game.rating
        }?.toDTO() ?: throw NoSuchElementException("Game not found with id: ${game.id}")
    }

    override suspend fun deleteGameById(id: ULong): Unit = suspendTransaction {
        val dao = GameDAO.findById(id) ?: return@suspendTransaction
        GameReleaseDAO.find { GameReleaseTable.gameId eq id }.forEach { it.delete() }
        GamePlayRecordDAO.find { GamePlayRecordTable.gameId eq id }.forEach { it.delete() }
        dao.delete()
    }

    override suspend fun selectGameReleasesByGameId(gameId: ULong): List<GameRelease> = suspendTransaction {
        GameReleaseDAO.find { GameReleaseTable.gameId eq gameId }.map { it.toDTO() }
    }

    override suspend fun insertGameRelease(release: GameRelease): GameRelease = suspendTransaction {
        GameReleaseDAO.new {
            gameId = release.gameId
            platform = release.platform
            releaseDate = release.releaseDate
            version = release.version
            language = release.language
        }.toDTO()
    }

    override suspend fun updateGameRelease(release: GameRelease): GameRelease = suspendTransaction {
        GameReleaseDAO.findByIdAndUpdate(release.id) {
            it.platform = release.platform
            it.releaseDate = release.releaseDate
            it.version = release.version
            it.language = release.language
        }?.toDTO() ?: throw NoSuchElementException("Game release not found with id: ${release.id}")
    }

    override suspend fun deleteGameReleaseById(id: ULong): Unit = suspendTransaction {
        GameReleaseDAO.findById(id)?.delete()
    }

    override suspend fun selectGamePlayRecordByGameId(gameId: ULong): GamePlayRecord? = suspendTransaction {
        GamePlayRecordDAO.find { GamePlayRecordTable.gameId eq gameId }.firstOrNull()?.toDTO()
    }

    override suspend fun upsertGamePlayRecord(record: GamePlayRecord): GamePlayRecord = suspendTransaction {
        val existing = GamePlayRecordDAO.find { GamePlayRecordTable.gameId eq record.gameId }.firstOrNull()
        if (existing != null) {
            existing.playStatus = record.playStatus
            existing.playTimeMinutes = record.playTimeMinutes
            existing.clearCount = record.clearCount
            existing.score = record.score
            existing.comment = record.comment
            existing.lastPlayedAt = record.lastPlayedAt?.let { java.time.OffsetDateTime.ofInstant(JavaInstant.ofEpochSecond(it.epochSeconds, it.nanosecondsOfSecond.toLong()), java.time.ZoneOffset.UTC) }
            existing.toDTO()
        } else {
            GamePlayRecordDAO.new {
                gameId = record.gameId
                playStatus = record.playStatus
                playTimeMinutes = record.playTimeMinutes
                clearCount = record.clearCount
                score = record.score
                comment = record.comment
                lastPlayedAt = record.lastPlayedAt?.let { java.time.OffsetDateTime.ofInstant(JavaInstant.ofEpochSecond(it.epochSeconds, it.nanosecondsOfSecond.toLong()), java.time.ZoneOffset.UTC) }
            }.toDTO()
        }
    }
}
