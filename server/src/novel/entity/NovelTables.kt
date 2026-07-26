package dev.sunriseydy.acgn.server.novel.db

import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelVolume
import dev.sunriseydy.acgn.novel.enums.NovelStatusEnum
import kotlin.time.Instant as KtInstant
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.dao.ULongEntity
import org.jetbrains.exposed.v1.dao.ULongEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object NovelTable : ULongIdTable("novel") {
    val name = varchar("name", 1024)
    val originalName = varchar("original_name", 1024).nullable()
    val author = varchar("author", 256).nullable()
    val illustrator = varchar("illustrator", 256).nullable()
    val description = text("description", eagerLoading = true).nullable()
    val publisher = varchar("publisher", 256).nullable()
    val status = varchar("status", 64).default(NovelStatusEnum.SERIALIZING.name)
    val totalVolumes = integer("total_volumes").default(0)
    val bgmId = ulong("bgm_id").nullable().uniqueIndex()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

class NovelDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<NovelDAO>(NovelTable)

    var name by NovelTable.name
    var originalName by NovelTable.originalName
    var author by NovelTable.author
    var illustrator by NovelTable.illustrator
    var description by NovelTable.description
    var publisher by NovelTable.publisher
    var status by NovelTable.status
    var totalVolumes by NovelTable.totalVolumes
    var bgmId by NovelTable.bgmId
    var createdAt by NovelTable.createdAt
    var updatedAt by NovelTable.updatedAt

    fun toDTO() = Novel(
        id = id.value,
        name = name,
        originalName = originalName,
        author = author,
        illustrator = illustrator,
        description = description,
        publisher = publisher,
        status = status,
        totalVolumes = totalVolumes,
        bgmId = bgmId,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

object NovelVolumeTable : ULongIdTable("novel_volume") {
    val novelId = ulong("novel_id")
    val volumeNumber = double("volume_number")
    val name = varchar("name", 1024)
    val description = text("description", eagerLoading = true).nullable()
    val releaseDate = date("release_date").nullable()
    val isbn = varchar("isbn", 64).nullable()
    val bgmId = ulong("bgm_id").nullable().uniqueIndex()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        uniqueIndex(novelId, volumeNumber)
    }
}

class NovelVolumeDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<NovelVolumeDAO>(NovelVolumeTable)

    var novelId by NovelVolumeTable.novelId
    var volumeNumber by NovelVolumeTable.volumeNumber
    var name by NovelVolumeTable.name
    var description by NovelVolumeTable.description
    var releaseDate by NovelVolumeTable.releaseDate
    var isbn by NovelVolumeTable.isbn
    var bgmId by NovelVolumeTable.bgmId
    var createdAt by NovelVolumeTable.createdAt
    var updatedAt by NovelVolumeTable.updatedAt

    fun toDTO() = NovelVolume(
        id = id.value,
        novelId = novelId,
        volumeNumber = volumeNumber,
        name = name,
        description = description,
        releaseDate = releaseDate,
        isbn = isbn,
        bgmId = bgmId,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

fun novelTables() = listOf(
    NovelTable,
    NovelVolumeTable,
)
