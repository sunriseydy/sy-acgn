package dev.sunriseydy.acgn.anime.dto

/**
 * 订阅 DTO
 */
data class Rss(
    val id: Long,
    val link: String,
    val title: String,
    val description: String,
    val ttl: Int,
    val lastFetchAt: String,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 订阅内容 DTO
 */
data class RssItem(
    val uuid: String,
    val rssId: Long,
    val link: String,
    val title: String,
    val description: String,
    val content: String,
    val torrent: String,
    val isRead: Boolean,
    val createdAt: String,
    val updatedAt: String
)