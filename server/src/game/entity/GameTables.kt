package dev.sunriseydy.acgn.server.game.db

import dev.sunriseydy.acgn.game.dto.Game
import dev.sunriseydy.acgn.game.dto.GamePlayRecord
import dev.sunriseydy.acgn.game.dto.GameRelease
import dev.sunriseydy.acgn.game.enums.GamePlatformEnum
import dev.sunriseydy.acgn.game.enums.GamePlayStatusEnum
import kotlin.time.Instant as KtInstant
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.dao.ULongEntity
import org.jetbrains.exposed.v1.dao.ULongEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object GameTable : ULongIdTable("game") {
    val name = varchar("name", 1024)
    val originalName = varchar("original_name", 1024).nullable()
    val developer = varchar("developer", 512).nullable()
    val publisher = varchar("publisher", 512).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val releaseDate = date("release_date").nullable()
    val bgmId = ulong("bgm_id").nullable().uniqueIndex()
    val steamId = ulong("steam_id").nullable().uniqueIndex()
    val rating = double("rating").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

class GameDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<GameDAO>(GameTable)

    var name by GameTable.name
    var originalName by GameTable.originalName
    var developer by GameTable.developer
    var publisher by GameTable.publisher
    var description by GameTable.description
    var releaseDate by GameTable.releaseDate
    var bgmId by GameTable.bgmId
    var steamId by GameTable.steamId
    var rating by GameTable.rating
    var createdAt by GameTable.createdAt
    var updatedAt by GameTable.updatedAt

    fun toDTO() = Game(
        id = id.value,
        name = name,
        originalName = originalName,
        developer = developer,
        publisher = publisher,
        description = description,
        releaseDate = releaseDate,
        bgmId = bgmId,
        steamId = steamId,
        rating = rating,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

object GameReleaseTable : ULongIdTable("game_release") {
    val gameId = ulong("game_id")
    val platform = varchar("platform", 64).default(GamePlatformEnum.STEAM.name)
    val releaseDate = date("release_date").nullable()
    val version = varchar("version", 256).nullable()
    val language = varchar("language", 256).nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

class GameReleaseDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<GameReleaseDAO>(GameReleaseTable)

    var gameId by GameReleaseTable.gameId
    var platform by GameReleaseTable.platform
    var releaseDate by GameReleaseTable.releaseDate
    var version by GameReleaseTable.version
    var language by GameReleaseTable.language
    var createdAt by GameReleaseTable.createdAt
    var updatedAt by GameReleaseTable.updatedAt

    fun toDTO() = GameRelease(
        id = id.value,
        gameId = gameId,
        platform = platform,
        releaseDate = releaseDate,
        version = version,
        language = language,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

object GamePlayRecordTable : ULongIdTable("game_play_record") {
    val gameId = ulong("game_id").uniqueIndex()
    val playStatus = varchar("play_status", 64).default(GamePlayStatusEnum.UNPLAYED.name)
    val playTimeMinutes = long("play_time_minutes").default(0L)
    val clearCount = integer("clear_count").default(0)
    val score = double("score").nullable()
    val comment = text("comment", eagerLoading = true).nullable()
    val lastPlayedAt = timestampWithTimeZone("last_played_at").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

class GamePlayRecordDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<GamePlayRecordDAO>(GamePlayRecordTable)

    var gameId by GamePlayRecordTable.gameId
    var playStatus by GamePlayRecordTable.playStatus
    var playTimeMinutes by GamePlayRecordTable.playTimeMinutes
    var clearCount by GamePlayRecordTable.clearCount
    var score by GamePlayRecordTable.score
    var comment by GamePlayRecordTable.comment
    var lastPlayedAt by GamePlayRecordTable.lastPlayedAt
    var createdAt by GamePlayRecordTable.createdAt
    var updatedAt by GamePlayRecordTable.updatedAt

    fun toDTO() = GamePlayRecord(
        id = id.value,
        gameId = gameId,
        playStatus = playStatus,
        playTimeMinutes = playTimeMinutes,
        clearCount = clearCount,
        score = score,
        comment = comment,
        lastPlayedAt = lastPlayedAt?.toInstant()?.let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

fun gameTables() = listOf(
    GameTable,
    GameReleaseTable,
    GamePlayRecordTable,
)
