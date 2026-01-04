@file:UseSerializers(OffsetDateTimeSerializer::class)
package dev.sunriseydy.acgn.anime.dto

import dev.sunriseydy.acgn.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * 订阅 DTO
 */
@Serializable
data class Rss(
    val id: ULong,
    val link: String,
    val title: String,
    val description: String? = null,
    val ttl: Int = 3600,
    val lastFetchAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
) {
    var items: List<RssItem> = emptyList()
    var unreadCount: Long = 0
}

/**
 * 订阅内容 DTO
 */
@Serializable
data class RssItem(
    val id: String,
    val rssId: ULong,
    val link: String,
    val guid: String,
    val title: String,
    val description: String? = null,
    val content: String? = null,
    val torrent: String,
    val isRead: Boolean = false,
    val publishedAt: OffsetDateTime,
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
) {
    var rss: Rss? = null
}

@Serializable
data class TorrentAdd(
    val url: String,
    val category: String? = null,
    val autoTMM: Boolean = true,
)