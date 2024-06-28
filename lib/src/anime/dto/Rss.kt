package dev.sunriseydy.acgn.anime.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * 订阅 DTO
 */
@Serializable
data class Rss(
    val id: ULong,
    val link: String,
    val title: String,
    val description: String?,
    val ttl: Int,
    val lastFetchAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val version: Int,
)

/**
 * 订阅内容 DTO
 */
@Serializable
data class RssItem(
    val uuid: String,
    val rssId: Long,
    val link: String,
    val title: String,
    val description: String,
    val content: String,
    val torrent: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val version: Int,
)
