package dev.sunriseydy.acgn.db.anime

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * @author SunriseYDY
 * @date 2024-06-28 00:11
 */
object RssTable : IntIdTable("anime_rss") {
    val link = varchar("link", 255)
    val title = varchar("title", 255)
    val description = varchar("description", 255)
    val ttl = integer("ttl")
    val lastFetchAt = date("lastFetchAt")
    val createdAt = date("createdAt")
    val updatedAt = date("updatedAt")
}