package dev.sunriseydy.acgn.anime.db

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import org.jetbrains.exposed.dao.ULongEntity
import org.jetbrains.exposed.dao.ULongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.ULongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AnimeTable : ULongIdTable("anime") {
    val name = varchar("name", 1024).uniqueIndex()
    val originalName = varchar("original_name", 1024).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val tmdbId = ulong("tmdb_id").nullable().uniqueIndex()
    val bgmId = ulong("bmg_id").nullable().uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class AnimeDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AnimeDAO>(AnimeTable)

    var name by AnimeTable.name
    var originalName by AnimeTable.originalName
    var description by AnimeTable.description
    var tmdbId by AnimeTable.tmdbId
    var bgmId by AnimeTable.bgmId
    var createdAt by AnimeTable.createdAt
    var updatedAt by AnimeTable.updatedAt

    fun toDTO() = Anime(
        id = id.value,
        name = name,
        originalName = originalName,
        description = description,
        tmdbId = tmdbId,
        bgmId = bgmId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

object AnimeSeasonTable : ULongIdTable("anime_season") {
    val animeId = ulong("anime_id")
    val name = varchar("name", 1024).uniqueIndex()
    val originalName = varchar("original_name", 1024).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val season = integer("season")
    val year = integer("year")
    val month = integer("month")
    val startedAt = timestamp("start_at").nullable()
    val endedAt = timestamp("ended_at").nullable()
    val tmdbId = ulong("tmdb_id").nullable().uniqueIndex()
    val bgmId = ulong("bmg_id").nullable().uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val un = uniqueIndex(animeId, season)
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
    var tmdbId by AnimeSeasonTable.tmdbId
    var bgmId by AnimeSeasonTable.bgmId
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
        tmdbId = tmdbId,
        bgmId = bgmId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

object AnimeEpisodeTable : ULongIdTable("anime_episode") {
    val animeId = ulong("anime_id")
    val animeSeasonId = ulong("anime_season_id")
    val name = varchar("name", 1024).uniqueIndex()
    val originalName = varchar("original_name", 1024).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val episode = integer("episode")
    val publishedAt = timestamp("published_at").nullable()
    val tmdbId = ulong("tmdb_id").nullable().uniqueIndex()
    val bgmId = ulong("bmg_id").nullable().uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val un = uniqueIndex(animeId, animeSeasonId, episode)
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
    var tmdbId by AnimeEpisodeTable.tmdbId
    var bgmId by AnimeEpisodeTable.bgmId
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
        tmdbId = tmdbId,
        bgmId = bgmId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun animeTables() = listOf(
    AnimeTable,
    AnimeSeasonTable,
    AnimeEpisodeTable,
)