package dev.sunriseydy.acgn.server.anime.db

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.ULongEntity
import org.jetbrains.exposed.v1.dao.ULongEntityClass
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-06-28 00:11
 */
object RssTable : ULongIdTable("anime_rss") {
    val link = varchar("link", 1024).uniqueIndex()
    val title = varchar("title", 255)
    val description = text("description", eagerLoading = true).nullable()
    val ttl = integer("ttl")
    val lastFetchAt = timestampWithTimeZone("last_fetch_at").defaultExpression(CurrentTimestampWithTimeZone)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

class RssDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<RssDAO>(RssTable)

    var link by RssTable.link
    var title by RssTable.title
    var description by RssTable.description
    var ttl by RssTable.ttl
    var lastFetchAt by RssTable.lastFetchAt
    var createdAt by RssTable.createdAt
    var updatedAt by RssTable.updatedAt

    fun toDTO(): Rss = Rss(
        id = id.value,
        link = link,
        title = title,
        description = description,
        ttl = ttl,
        lastFetchAt = lastFetchAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

object RssItemTable : UUIDTable("anime_rss_item") {
    val rssId = ulong("rss_id")
    val link = varchar("link", 1024)
    val guid = varchar("guid", 1024)
    val title = varchar("title", 255)
    val description = text("description", eagerLoading = true).nullable()
    val content = text("content", eagerLoading = true).nullable()
    val torrent = text("torrent", eagerLoading = true)
    val isRead = bool("is_read")
    val publishedAt = timestampWithTimeZone("published_at").index()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        uniqueIndex(rssId, guid)
    }
}

class RssItemDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RssItemDAO>(RssItemTable)

    var rssId by RssItemTable.rssId
    var link by RssItemTable.link
    var guid by RssItemTable.guid
    var title by RssItemTable.title
    var description by RssItemTable.description
    var content by RssItemTable.content
    var torrent by RssItemTable.torrent
    var isRead by RssItemTable.isRead
    var publishedAt by RssItemTable.publishedAt
    var createdAt by RssItemTable.createdAt
    var updatedAt by RssItemTable.updatedAt

    fun toDTO(): RssItem = RssItem(
        id = id.value.toString(),
        rssId = rssId,
        link = link,
        guid = guid,
        title = title,
        description = description.toString(),
        content = content.toString(),
        torrent = torrent,
        isRead = isRead,
        publishedAt = publishedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun rssTables() = listOf(RssTable, RssItemTable)