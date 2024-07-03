package dev.sunriseydy.acgn.anime.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 订阅 DTO
 */
@Serializable
data class Rss(
    var id: ULong? = null,
    var link: String,
    var title: String? = null,
    var description: String? = null,
    var ttl: Int? = 1800,
    var lastFetchAt: Instant? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    var items: List<RssItem>? = null
) {
    constructor() : this(
        link = "",
    )
}

/**
 * 订阅内容 DTO
 */
@Serializable
data class RssItem(
    var id: String? = null,
    var rssId: ULong? = null,
    var link: String,
    var guid: String,
    var title: String,
    var description: String? = null,
    var content: String? = null,
    var torrent: String,
    var isRead: Boolean = false,
    var publishedAt: Instant = Clock.System.now(),
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    constructor() : this(
        link = "",
        guid = "",
        title = "",
        torrent = "",
    )
}
