package dev.sunriseydy.acgn.anime.dto

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable
/**
 * RSS 订阅源 DTO
 *
 * @property id 订阅源 ID
 * @property link 订阅源链接
 * @property title 订阅源标题
 * @property ttl 刷新间隔（秒）
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
    /** 订阅源下的内容列表 */
    var items: List<RssItem> = emptyList()

    /** 未读条目数量 */
    var unreadCount: Long = 0
}

/**
 * RSS 订阅内容 DTO
 *
 * @property id 内容 ID
 * @property rssId 所属订阅源 ID
 * @property torrent Torrent 下载链接
 * @property isRead 是否已读
 * @property publishedAt 发布时间
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
) {
    /** 所属的 RSS 订阅源（延迟关联） */
    var rss: Rss? = null
}

/**
 * Torrent 添加请求 DTO
 *
 * @property url Torrent 下载链接或磁力链接
 * @property category 分类（可选）
 * @property autoTMM 是否启用自动 Torrent 管理
 */
@Serializable
data class TorrentAdd(
    val url: String,
    val category: String? = null,
    val autoTMM: Boolean = true,
)