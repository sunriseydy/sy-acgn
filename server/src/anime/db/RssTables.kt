package dev.sunriseydy.acgn.anime.db

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import org.jetbrains.exposed.dao.ULongEntity
import org.jetbrains.exposed.dao.ULongEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.ULongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-06-28 00:11
 */
object RssTable : ULongIdTable("anime_rss") {
    val link = varchar("link", 255)
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val ttl = integer("ttl")
    val lastFetchAt = datetime("lastFetchAt").nullable()
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updatedAt").defaultExpression(CurrentDateTime)
    val version = integer("version").default(0)
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
    var version by RssTable.version

    fun toDTO(): Rss {
        return Rss(
            id = id.value,
            link = link,
            title = title,
            description = description,
            ttl = ttl,
            lastFetchAt = lastFetchAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version,
        )
    }
}

object RssItemTable : UUIDTable("anime_rss_item", "uuid") {
    val rssId = long("rss_id")
    val link = varchar("link", 255)
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val content = text("content").nullable()
    val torrent = varchar("torrent", 255)
    val isRead = bool("is_read")
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updatedAt").defaultExpression(CurrentDateTime)
    val version = integer("version").default(0)
}

class RssItemDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RssItemDAO>(RssItemTable)

    var rssId by RssItemTable.rssId
    var link by RssItemTable.link
    var title by RssItemTable.title
    var description by RssItemTable.description
    var content by RssItemTable.content
    var torrent by RssItemTable.torrent
    var isRead by RssItemTable.isRead
    var createdAt by RssItemTable.createdAt
    var updatedAt by RssItemTable.updatedAt
    var version by RssItemTable.version

    fun toDTO(): RssItem {
        return RssItem(
            uuid = id.value.toString(),
            rssId = rssId,
            link = link,
            title = title,
            description = description.toString(),
            content = content.toString(),
            torrent = torrent,
            isRead = isRead,
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version
        )
    }
}