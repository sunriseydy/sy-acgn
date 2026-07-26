package dev.sunriseydy.acgn.server.anime.tools.bangumi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BangumiSubject(
    val id: Int,
    val type: Int,
    val name: String,
    @SerialName("name_cn") val nameCn: String,
    val summary: String,
    val series: Boolean = false,
    val nsfw: Boolean = false,
    val locked: Boolean = false,
    val date: String? = null,
    val platform: String? = null,
    val images: BangumiImages? = null,
    val infobox: List<BangumiWiki>? = null,
    val volumes: Int = 0,
    val eps: Int = 0,
    @SerialName("total_episodes") val totalEpisodes: Int = 0,
    val rating: BangumiRating? = null,
    val collection: BangumiCollection? = null,
    val tags: List<BangumiTag> = emptyList()
)

@Serializable
data class BangumiWiki(
    val key: String,
    val value: kotlinx.serialization.json.JsonElement
)

@Serializable
data class BangumiSearchRequest(
    val keyword: String,
    val sort: String? = "match",
    val filter: BangumiSearchFilter? = null
)

@Serializable
data class BangumiSearchFilter(
    val type: List<Int>? = null,
    @SerialName("meta_tags") val metaTags: List<String>? = null,
    val tag: List<String>? = null,
    @SerialName("air_date") val airDate: List<String>? = null,
    val rating: List<String>? = null,
    @SerialName("rating_count") val ratingCount: List<String>? = null,
    val rank: List<String>? = null,
    val nsfw: Boolean? = null
)

@Serializable
data class BangumiImages(
    val large: String,
    val common: String,
    val medium: String,
    val small: String,
    val grid: String
)

@Serializable
data class BangumiRating(
    val rank: Int,
    val total: Int,
    val count: Map<String, Int> = emptyMap(),
    val score: Double
)

@Serializable
data class BangumiCollection(
    val wish: Int,
    val collect: Int,
    val doing: Int,
    @SerialName("on_hold") val onHold: Int,
    val dropped: Int
)

@Serializable
data class BangumiTag(
    val name: String,
    val count: Int
)

@Serializable
data class BangumiSearchResponse(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val data: List<BangumiSubject>
)

@Serializable
data class BangumiRelatedSubject(
    val id: Int,
    val type: Int,
    val name: String,
    @SerialName("name_cn") val nameCn: String = "",
    val relation: String = "",
    val images: BangumiImages? = null
)

