package dev.sunriseydy.acgn.anime.db

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeAddition
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import org.jetbrains.exposed.dao.ULongEntity
import org.jetbrains.exposed.dao.ULongEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.ULongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.UUID

object AnimeTable : ULongIdTable("anime") {
    val name = varchar("name", 1024)
    val originalName = varchar("original_name", 1024).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class AnimeDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AnimeDAO>(AnimeTable)

    var name by AnimeTable.name
    var originalName by AnimeTable.originalName
    var description by AnimeTable.description
    var createdAt by AnimeTable.createdAt
    var updatedAt by AnimeTable.updatedAt

    fun toDTO() = Anime(
        id = id.value,
        name = name,
        originalName = originalName,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

object AnimeSeasonTable : ULongIdTable("anime_season") {
    val animeId = ulong("anime_id")
    val name = varchar("name", 1024)
    val originalName = varchar("original_name", 1024).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val season = integer("season")
    val year = integer("year")
    val month = integer("month")
    val startedAt = timestamp("start_at").nullable()
    val endedAt = timestamp("ended_at").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

}

class AnimeSeasonDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AnimeSeasonDAO>(AnimeSeasonTable)

    var animeId by AnimeSeasonTable.animeId
    var name by AnimeSeasonTable.name
    var originalName by AnimeSeasonTable.originalName
    var description by AnimeSeasonTable.description
    var season by AnimeSeasonTable.season
    var year by AnimeSeasonTable.year
    var month by AnimeSeasonTable.month
    var startedAt by AnimeSeasonTable.startedAt
    var endedAt by AnimeSeasonTable.endedAt
    var createdAt by AnimeSeasonTable.createdAt
    var updatedAt by AnimeSeasonTable.updatedAt

    fun toDTO() = AnimeSeason(
        id = id.value,
        animeId = animeId,
        name = name,
        originalName = originalName,
        description = description,
        season = season,
        year = year,
        month = month,
        startedAt = startedAt,
        endedAt = endedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

object AnimeEpisodeTable : ULongIdTable("anime_episode") {
    val animeId = ulong("anime_id")
    val animeSeasonId = ulong("anime_season_id")
    val name = varchar("name", 1024)
    val originalName = varchar("original_name", 1024).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val episode = integer("episode")
    val publishedAt = timestamp("published_at").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

}

class AnimeEpisodeDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AnimeEpisodeDAO>(AnimeEpisodeTable)

    var animeId by AnimeEpisodeTable.animeId
    var animeSeasonId by AnimeEpisodeTable.animeSeasonId
    var name by AnimeEpisodeTable.name
    var originalName by AnimeEpisodeTable.originalName
    var description by AnimeEpisodeTable.description
    var episode by AnimeEpisodeTable.episode
    var publishedAt by AnimeEpisodeTable.publishedAt
    var createdAt by AnimeEpisodeTable.createdAt
    var updatedAt by AnimeEpisodeTable.updatedAt

    fun toDTO() = AnimeEpisode(
        id = id.value,
        animeId = animeId,
        animeSeasonId = animeSeasonId,
        name = name,
        originalName = originalName,
        description = description,
        episode = episode,
        publishedAt = publishedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

object AnimeAdditionTable : UUIDTable("anime_addition") {
    val associatedId = ulong("associated_id")
    val associatedType = varchar("associated_type", 256)
    val additionalType = varchar("additional_type", 256)
    val value = text("value", eagerLoading = true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class AnimeAdditionDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AnimeAdditionDAO>(AnimeAdditionTable)

    var associatedId by AnimeAdditionTable.associatedId
    var associatedType by AnimeAdditionTable.associatedType
    var additionalType by AnimeAdditionTable.additionalType
    var value by AnimeAdditionTable.value
    var createdAt by AnimeAdditionTable.createdAt
    var updatedAt by AnimeAdditionTable.updatedAt

    fun toDTO() = AnimeAddition(
        id = id.toString(),
        associatedId = associatedId,
        associatedType = AnimeAssociatedType.valueOf(associatedType),
        additionalType = AnimeAdditionType.valueOf(additionalType),
        value = value,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun animeTables() = listOf(
    AnimeTable,
    AnimeSeasonTable,
    AnimeEpisodeTable,
    AnimeAdditionTable,
)