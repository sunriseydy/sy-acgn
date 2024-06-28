package dev.sunriseydy.acgn.db.anime

import org.jetbrains.exposed.dao.id.ULongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * @author SunriseYDY
 * @date 2024-06-28 00:11
 */
object RssTable : ULongIdTable("anime_rss") {
    val link = varchar("link", 255)
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val ttl = integer("ttl")
    val lastFetchAt = datetime("lastFetchAt")
    val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updatedAt").defaultExpression(CurrentDateTime)
    val version = integer("version").default(0)
}