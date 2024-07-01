package dev.sunriseydy.acgn.anime.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 订阅 DTO
 */
@Serializable
data class Rss(
    var id: ULong?,
    var link: String,
    var title: String,
    var description: String?,
    var ttl: Int?,
    var lastFetchAt: Instant?,
    var createdAt: Instant?,
    var updatedAt: Instant?,
    var version: Int?,
    var items: List<RssItem>? = null
) {
    constructor() : this(
        id = null,
        link = "",
        title = "",
        description = null,
        ttl = null,
        lastFetchAt = null,
        createdAt = null,
        updatedAt = null,
        version = null,
    )
}

/**
 * 订阅内容 DTO
 */
@Serializable
data class RssItem(
    var uuid: String?,
    var rssId: Long?,
    var link: String,
    var guid: String,
    var title: String,
    var description: String?,
    var content: String?,
    var torrent: String,
    var isRead: Boolean,
    var publishedAt: Instant,
    var createdAt: Instant?,
    var updatedAt: Instant?,
    var version: Int?,
) {
    constructor() : this(
        uuid = null,
        rssId = null,
        link = "",
        guid = "",
        title = "",
        description = null,
        content = null,
        torrent = "",
        isRead = false,
        publishedAt = Clock.System.now(),
        createdAt = null,
        updatedAt = null,
        version = null,
    )
}
