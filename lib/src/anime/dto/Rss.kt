package dev.sunriseydy.acgn.anime.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
    val lastFetchAt: Instant = Clock.System.now(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
) {
    var items: List<RssItem> = emptyList()
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
    val publishedAt: Instant,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
)

@Serializable
data class TorrentAdd(
    val url: String,
    val category: String?,
    val autoTMM: Boolean = true,
)