package dev.sunriseydy.acgn.server.game.service

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.game.dto.*
import dev.sunriseydy.acgn.game.enums.GameAdditionType
import dev.sunriseydy.acgn.game.enums.GameAssociatedType
import dev.sunriseydy.acgn.server.anime.tools.BangumiTool
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository
import dev.sunriseydy.acgn.server.common.service.AttachFileInfoService
import dev.sunriseydy.acgn.server.game.repository.GameRepository
import dev.sunriseydy.acgn.server.game.tools.GameCacheTool
import dev.sunriseydy.acgn.server.game.tools.SteamTool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GameServiceImpl(
    private val gameRepository: GameRepository,
    private val additionalInfoRepository: AdditionalInfoRepository,
    private val bangumiTool: BangumiTool,
    private val steamTool: SteamTool,
    private val attachFileInfoService: AttachFileInfoService
) : GameService {

    private val logger = KotlinLogging.logger { }

    override suspend fun getGameList(
        fromDb: Boolean,
        name: String?,
        platform: String?,
        playStatus: String?
    ): List<Game> = coroutineScope {
        if (fromDb || GameCacheTool.isGameEmpty()) {
            val games = gameRepository.selectAllGame(name, platform, playStatus)
            games.map {
                val fullGame = attachFullGameDetails(it)
                GameCacheTool.setGame(fullGame)
            }
        } else {
            var cached = GameCacheTool.getGameList()
            if (!name.isNullOrBlank()) {
                cached = cached.filter {
                    it.name.contains(name, ignoreCase = true) ||
                            (it.originalName?.contains(name, ignoreCase = true) == true)
                }
            }
            if (!platform.isNullOrBlank()) {
                cached = cached.filter { game ->
                    game.releases.any { it.platform.equals(platform, ignoreCase = true) }
                }
            }
            if (!playStatus.isNullOrBlank()) {
                cached = cached.filter { game ->
                    game.playRecord?.playStatus.equals(playStatus, ignoreCase = true)
                }
            }
            return@coroutineScope cached
        }
    }

    override suspend fun getGameById(id: ULong): Game {
        val cached = GameCacheTool.getGameById(id)
        if (cached != null) return cached

        val game = gameRepository.selectGameById(id)
        val fullGame = attachFullGameDetails(game)
        GameCacheTool.setGame(fullGame)
        return fullGame
    }

    private suspend fun attachFullGameDetails(game: Game): Game {
        val releases = gameRepository.selectGameReleasesByGameId(game.id)
        val playRecord = gameRepository.selectGamePlayRecordByGameId(game.id)
        val additions = additionalInfoRepository.selectAdditionalInfos(
            associatedType = GameAssociatedType.GAME.key,
            associatedId = game.id
        )
        return game.copy(
            releases = releases,
            playRecord = playRecord,
            additions = additions
        )
    }

    private suspend fun downloadAndSavePoster(
        gameId: ULong,
        additions: List<AdditionalInfo>,
        forceUpdate: Boolean = false
    ): AdditionalInfo? {
        val bgmJson = GameAdditionType.BgmJson.valueOf(additions)
        val steamJson = GameAdditionType.SteamJson.valueOf(additions)

        val bgmImageUrl = bgmJson?.get("images")?.jsonObject?.let {
            it["common"]?.jsonPrimitive?.contentOrNull
                ?: it["large"]?.jsonPrimitive?.contentOrNull
                ?: it["medium"]?.jsonPrimitive?.contentOrNull
        }
        val steamImageUrl = steamJson?.get("header_image")?.jsonPrimitive?.contentOrNull
            ?: steamJson?.get("capsule_image")?.jsonPrimitive?.contentOrNull

        val imageUrl = bgmImageUrl ?: steamImageUrl
        if (imageUrl.isNullOrBlank()) return null

        val existingPosterId = GameAdditionType.PosterId.valueOf(additions)
        if (!existingPosterId.isNullOrBlank() && !forceUpdate) {
            return null
        }

        try {
            val attachFileId = attachFileInfoService.saveFile(
                downloadUrl = imageUrl,
                defaultContentType = "image/jpeg",
                defaultFileName = imageUrl.substringAfterLast("/").substringBefore("?").ifBlank { "poster.jpg" }
            )

            if (!existingPosterId.isNullOrBlank()) {
                try {
                    attachFileInfoService.deleteFile(existingPosterId)
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to delete old game poster attach file $existingPosterId" }
                }
                additionalInfoRepository.deleteAdditionalInfos(
                    associatedType = GameAssociatedType.GAME.key,
                    associatedId = gameId,
                    additionalType = GameAdditionType.PosterId.key
                )
            }

            val posterAddition = AdditionalInfo(
                id = "",
                associatedId = gameId,
                associatedType = GameAssociatedType.GAME.key,
                additionalType = GameAdditionType.PosterId.key,
                additionalValue = attachFileId
            )
            additionalInfoRepository.saveAdditionalInfos(
                listOf(posterAddition),
                associatedId = gameId
            )
            return posterAddition
        } catch (e: Exception) {
            logger.error(e) { "Error downloading poster for game $gameId from $imageUrl" }
        }
        return null
    }

    override suspend fun createGame(dto: GameCreateOrUpdateDto): Game {
        val newGame = Game(
            id = ULong.MIN_VALUE,
            name = dto.name,
            originalName = dto.originalName,
            developer = dto.developer,
            publisher = dto.publisher,
            description = dto.description,
            releaseDate = dto.releaseDate,
            bgmId = dto.bgmId,
            steamId = dto.steamId,
            rating = dto.rating
        )
        val created = gameRepository.insertGame(newGame)
        val full = attachFullGameDetails(created)
        GameCacheTool.setGame(full)
        return full
    }

    override suspend fun updateGame(dto: GameCreateOrUpdateDto): Game {
        val dtoId = requireNotNull(dto.id) { "Game ID must not be null for update" }
        val existing = gameRepository.selectGameById(dtoId)
        val toUpdate = existing.copy(
            name = dto.name,
            originalName = dto.originalName,
            developer = dto.developer,
            publisher = dto.publisher,
            description = dto.description,
            releaseDate = dto.releaseDate,
            bgmId = dto.bgmId,
            steamId = dto.steamId,
            rating = dto.rating
        )
        val updated = gameRepository.updateGame(toUpdate)
        val full = attachFullGameDetails(updated)
        GameCacheTool.setGame(full)
        return full
    }

    override suspend fun deleteGame(id: ULong) {
        val additions = additionalInfoRepository.selectAdditionalInfos(
            associatedType = GameAssociatedType.GAME.key,
            associatedId = id
        )
        val posterId = GameAdditionType.PosterId.valueOf(additions)
        if (!posterId.isNullOrBlank()) {
            try {
                attachFileInfoService.deleteFile(posterId)
            } catch (e: Exception) {
                logger.warn(e) { "Failed to delete poster attach file $posterId for game $id" }
            }
        }
        gameRepository.deleteGameById(id)
        additionalInfoRepository.deleteAdditionalInfos(
            associatedType = GameAssociatedType.GAME.key,
            associatedId = id
        )
        GameCacheTool.removeGame(id)
    }

    override suspend fun getReleasesByGameId(gameId: ULong): List<GameRelease> {
        return gameRepository.selectGameReleasesByGameId(gameId)
    }

    override suspend fun createRelease(dto: GameReleaseCreateOrUpdateDto): GameRelease {
        val release = GameRelease(
            id = ULong.MIN_VALUE,
            gameId = dto.gameId,
            platform = dto.platform,
            releaseDate = dto.releaseDate,
            version = dto.version,
            language = dto.language
        )
        val created = gameRepository.insertGameRelease(release)
        GameCacheTool.removeGame(dto.gameId)
        return created
    }

    override suspend fun updateRelease(dto: GameReleaseCreateOrUpdateDto): GameRelease {
        val releaseId = requireNotNull(dto.id) { "Release ID must not be null for update" }
        val release = GameRelease(
            id = releaseId,
            gameId = dto.gameId,
            platform = dto.platform,
            releaseDate = dto.releaseDate,
            version = dto.version,
            language = dto.language
        )
        val updated = gameRepository.updateGameRelease(release)
        GameCacheTool.removeGame(dto.gameId)
        return updated
    }

    override suspend fun deleteRelease(releaseId: ULong) {
        gameRepository.deleteGameReleaseById(releaseId)
    }

    override suspend fun getPlayRecord(gameId: ULong): GamePlayRecord? {
        return gameRepository.selectGamePlayRecordByGameId(gameId)
    }

    override suspend fun updatePlayRecord(dto: GamePlayRecordCreateOrUpdateDto): GamePlayRecord {
        val record = GamePlayRecord(
            id = dto.id ?: ULong.MIN_VALUE,
            gameId = dto.gameId,
            playStatus = dto.playStatus,
            playTimeMinutes = dto.playTimeMinutes,
            clearCount = dto.clearCount,
            score = dto.score,
            comment = dto.comment,
            lastPlayedAt = dto.lastPlayedAt
        )
        val updated = gameRepository.upsertGamePlayRecord(record)
        GameCacheTool.removeGame(dto.gameId)
        return updated
    }

    override suspend fun searchBangumiGame(query: String): List<Game> {
        return bangumiTool.searchGame(query)
    }

    override suspend fun importFromBangumi(bgmId: ULong, isUpdate: Boolean): Game {
        val bgmGame = bangumiTool.getGameSubject(bgmId.toInt())
        val existing = gameRepository.selectGameByBgmId(bgmId)

        return if (existing != null) {
            if (isUpdate) {
                val toUpdate = existing.copy(
                    name = bgmGame.name,
                    originalName = bgmGame.originalName,
                    developer = bgmGame.developer ?: existing.developer,
                    publisher = bgmGame.publisher ?: existing.publisher,
                    description = bgmGame.description ?: existing.description,
                    releaseDate = bgmGame.releaseDate ?: existing.releaseDate,
                    rating = bgmGame.rating ?: existing.rating
                )
                val updated = gameRepository.updateGame(toUpdate)

                val newBgmJson = bgmGame.additions.firstOrNull { it.additionalType == GameAdditionType.BgmJson.key }
                if (newBgmJson != null) {
                    additionalInfoRepository.deleteAdditionalInfos(
                        associatedType = GameAssociatedType.GAME.key,
                        associatedId = updated.id,
                        additionalType = GameAdditionType.BgmJson.key
                    )
                    additionalInfoRepository.saveAdditionalInfos(
                        listOf(newBgmJson.copy(associatedId = updated.id)),
                        associatedId = updated.id
                    )
                }

                val currentAdditions = additionalInfoRepository.selectAdditionalInfos(
                    associatedType = GameAssociatedType.GAME.key,
                    associatedId = updated.id
                )
                downloadAndSavePoster(updated.id, currentAdditions, forceUpdate = true)

                val full = attachFullGameDetails(updated)
                GameCacheTool.setGame(full)
                full
            } else {
                attachFullGameDetails(existing)
            }
        } else {
            val created = gameRepository.insertGame(bgmGame)
            if (bgmGame.additions.isNotEmpty()) {
                additionalInfoRepository.saveAdditionalInfos(bgmGame.additions, associatedId = created.id)
            }
            val currentAdditions = additionalInfoRepository.selectAdditionalInfos(
                associatedType = GameAssociatedType.GAME.key,
                associatedId = created.id
            )
            downloadAndSavePoster(created.id, currentAdditions, forceUpdate = false)

            val full = attachFullGameDetails(created)
            GameCacheTool.setGame(full)
            full
        }
    }

    override suspend fun searchSteamGame(query: String): List<Game> {
        return steamTool.searchGame(query)
    }

    override suspend fun importFromSteam(appId: ULong, isUpdate: Boolean): Game {
        val steamGame = steamTool.getAppDetail(appId)
        val existing = gameRepository.selectGameBySteamId(appId)

        return if (existing != null) {
            if (isUpdate) {
                val toUpdate = existing.copy(
                    name = steamGame.name,
                    developer = steamGame.developer ?: existing.developer,
                    publisher = steamGame.publisher ?: existing.publisher,
                    description = steamGame.description ?: existing.description
                )
                val updated = gameRepository.updateGame(toUpdate)

                val newSteamJson = steamGame.additions.firstOrNull { it.additionalType == GameAdditionType.SteamJson.key }
                if (newSteamJson != null) {
                    additionalInfoRepository.deleteAdditionalInfos(
                        associatedType = GameAssociatedType.GAME.key,
                        associatedId = updated.id,
                        additionalType = GameAdditionType.SteamJson.key
                    )
                    additionalInfoRepository.saveAdditionalInfos(
                        listOf(newSteamJson.copy(associatedId = updated.id)),
                        associatedId = updated.id
                    )
                }

                val currentAdditions = additionalInfoRepository.selectAdditionalInfos(
                    associatedType = GameAssociatedType.GAME.key,
                    associatedId = updated.id
                )
                downloadAndSavePoster(updated.id, currentAdditions, forceUpdate = true)

                val full = attachFullGameDetails(updated)
                GameCacheTool.setGame(full)
                full
            } else {
                attachFullGameDetails(existing)
            }
        } else {
            val created = gameRepository.insertGame(steamGame)
            if (steamGame.releases.isNotEmpty()) {
                steamGame.releases.forEach { rel ->
                    gameRepository.insertGameRelease(rel.copy(gameId = created.id))
                }
            }
            if (steamGame.additions.isNotEmpty()) {
                additionalInfoRepository.saveAdditionalInfos(steamGame.additions, associatedId = created.id)
            }
            val currentAdditions = additionalInfoRepository.selectAdditionalInfos(
                associatedType = GameAssociatedType.GAME.key,
                associatedId = created.id
            )
            downloadAndSavePoster(created.id, currentAdditions, forceUpdate = false)

            val full = attachFullGameDetails(created)
            GameCacheTool.setGame(full)
            full
        }
    }
}
