package dev.sunriseydy.acgn.server.anime.db

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import kotlin.time.Instant as KtInstant
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.dao.ULongEntity
import org.jetbrains.exposed.v1.dao.ULongEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object AnimeTable : ULongIdTable("anime") {
    val name = varchar("name", 1024).uniqueIndex()
    val description = text("description", eagerLoading = true).nullable()
    val firstAirDate = date("first_air_date").nullable()
    val lastAirDate = date("last_air_date").nullable()
    val numberOfSeasons = integer("number_of_seasons").default(0)
    val numberOfEpisodes = integer("number_of_episodes").default(0)
    val tmdbId = ulong("tmdb_id").nullable().uniqueIndex()
    val bgmId = ulong("bgm_id").nullable().uniqueIndex()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

class AnimeDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AnimeDAO>(AnimeTable)

    var name by AnimeTable.name
    var description by AnimeTable.description
    var firstAirDate by AnimeTable.firstAirDate
    var lastAirDate by AnimeTable.lastAirDate
    var numberOfSeasons by AnimeTable.numberOfSeasons
    var numberOfEpisodes by AnimeTable.numberOfEpisodes
    var tmdbId by AnimeTable.tmdbId
    var bgmId by AnimeTable.bgmId
    var createdAt by AnimeTable.createdAt
    var updatedAt by AnimeTable.updatedAt

    fun toDTO() = Anime(
        id = id.value,
        name = name,
        description = description,
        firstAirDate = firstAirDate,
        lastAirDate = lastAirDate,
        numberOfSeasons = numberOfSeasons,
        numberOfEpisodes = numberOfEpisodes,
        tmdbId = tmdbId,
        bgmId = bgmId,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

object AnimeSeasonTable : ULongIdTable("anime_season") {
    val animeId = ulong("anime_id")
    val name = varchar("name", 1024)
    val description = text("description", eagerLoading = true).nullable()
    val season = integer("season")
    val numberOfEpisodes = integer("number_of_episodes").default(0)
    val year = integer("year")
    val month = integer("month")
    val airDate = date("air_date").nullable()
    val tmdbId = ulong("tmdb_id").nullable().uniqueIndex()
    val bgmId = ulong("bgm_id").nullable().uniqueIndex()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        uniqueIndex(animeId, season)
    }
}

class AnimeSeasonDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AnimeSeasonDAO>(AnimeSeasonTable)

    var animeId by AnimeSeasonTable.animeId
    var name by AnimeSeasonTable.name
    var description by AnimeSeasonTable.description
    var season by AnimeSeasonTable.season
    var numberOfEpisodes by AnimeSeasonTable.numberOfEpisodes
    var year by AnimeSeasonTable.year
    var month by AnimeSeasonTable.month
    var airDate by AnimeSeasonTable.airDate
    var tmdbId by AnimeSeasonTable.tmdbId
    var bgmId by AnimeSeasonTable.bgmId
    var createdAt by AnimeSeasonTable.createdAt
    var updatedAt by AnimeSeasonTable.updatedAt

    fun toDTO() = AnimeSeason(
        id = id.value,
        animeId = animeId,
        name = name,
        description = description,
        season = season,
        numberOfEpisodes = numberOfEpisodes,
        year = year,
        month = month,
        airDate = airDate,
        tmdbId = tmdbId,
        bgmId = bgmId,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

object AnimeEpisodeTable : ULongIdTable("anime_episode") {
    val animeId = ulong("anime_id")
    val animeSeasonId = ulong("anime_season_id")
    // 集数名常重复（如“第1集”），唯一性由 (animeId, animeSeasonId, episode) 保证
    val name = varchar("name", 1024)
    val description = text("description", eagerLoading = true).nullable()
    val episode = integer("episode")
    val airDate = date("air_date").nullable()
    val tmdbId = ulong("tmdb_id").nullable().uniqueIndex()
    val bgmId = ulong("bgm_id").nullable().uniqueIndex()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        uniqueIndex(animeId, animeSeasonId, episode)
    }
}

class AnimeEpisodeDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AnimeEpisodeDAO>(AnimeEpisodeTable)

    var animeId by AnimeEpisodeTable.animeId
    var animeSeasonId by AnimeEpisodeTable.animeSeasonId
    var name by AnimeEpisodeTable.name
    var description by AnimeEpisodeTable.description
    var episode by AnimeEpisodeTable.episode
    var airDate by AnimeEpisodeTable.airDate
    var tmdbId by AnimeEpisodeTable.tmdbId
    var bgmId by AnimeEpisodeTable.bgmId
    var createdAt by AnimeEpisodeTable.createdAt
    var updatedAt by AnimeEpisodeTable.updatedAt

    fun toDTO() = AnimeEpisode(
        id = id.value,
        animeId = animeId,
        animeSeasonId = animeSeasonId,
        name = name,
        description = description,
        episode = episode,
        airDate = airDate,
        tmdbId = tmdbId,
        bgmId = bgmId,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

fun animeTables() = listOf(
    AnimeTable,
    AnimeSeasonTable,
    AnimeEpisodeTable,
)