package dev.sunriseydy.acgn.server.anime.tools.qbittorrent.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * qBittorrent API 数据模型
 *
 * 包含与 qBittorrent WebUI API 交互所需的所有序列化数据类。
 *
 * @author SunriseYDY
 * @date 2024-07-20 16:03
 */

/** Torrent 详情信息 */
@Serializable
data class TorrentInfo(
    @SerialName("completion_date") val completionDate: Long,
    @SerialName("download_path") val downloadPath: String,
    @SerialName("eta") val eta: Long,
    @SerialName("hash") val hash: String,
    @SerialName("name") val name: String,
    @SerialName("save_path") val savePath: String,
)

/** RSS 订阅源项 */
@Serializable
data class QbRssItem(
    @SerialName("uid") val uid: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("lastBuildDate") val lastBuildDate: String? = null,
    @SerialName("isLoading") val isLoading: Boolean? = null,
    @SerialName("hasError") val hasError: Boolean? = null,
    @SerialName("articles") val articles: List<QbRssArticle>? = null,
)

/** RSS 文章 */
@Serializable
data class QbRssArticle(
    @SerialName("id") val id: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("author") val author: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("torrentURL") val torrentURL: String? = null,
    @SerialName("link") val link: String? = null,
    @SerialName("isRead") val isRead: Boolean? = null,
)

/** RSS 自动下载规则 */
@Serializable
data class QbRssAutoDownloadingRule(
    @SerialName("enabled") val enabled: Boolean? = null,
    @SerialName("mustContain") val mustContain: String? = null,
    @SerialName("mustNotContain") val mustNotContain: String? = null,
    @SerialName("useRegex") val useRegex: Boolean? = null,
    @SerialName("episodeFilter") val episodeFilter: String? = null,
    @SerialName("smartFilter") val smartFilter: Boolean? = null,
    @SerialName("previouslyMatchedEpisodes") val previouslyMatchedEpisodes: List<String>? = null,
    @SerialName("affectedFeeds") val affectedFeeds: List<String>? = null,
    @SerialName("ignoreDays") val ignoreDays: Int? = null,
    @SerialName("lastMatch") val lastMatch: String? = null,
    @SerialName("addPaused") val addPaused: Boolean? = null,
    @SerialName("assignedCategory") val assignedCategory: String? = null,
    @SerialName("savePath") val savePath: String? = null,
) {
    fun toJsonString(): String {
        return Json.encodeToString(serializer(), this)
    }
}
