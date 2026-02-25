package dev.sunriseydy.acgn.client.anime.api

import dev.sunriseydy.acgn.anime.AnimeModuleResource
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.dto.TorrentAdd
import dev.sunriseydy.acgn.base.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * @author SunriseYDY
 * @date 2024-07-23 11:33
 */
class RssApi internal constructor(private val httpClient: HttpClient) {
    suspend fun addQbTorrent(torrentAdd: TorrentAdd): Result<String> =
        httpClient.post(AnimeModuleResource.Qb.Torrent()) {
            setBody(torrentAdd)
        }.body()
    

    // qBittorrent RSS API

    suspend fun getQbRssList(withData: Boolean = false): Result<List<Rss>> =
        httpClient.get(AnimeModuleResource.Qb.Rss.List(withData = withData)).body()
    

    suspend fun getQbRssArticles(
        rssId: ULong?,
        isRead: Boolean?,
        page: Long,
        size: Int,
    ): Result<List<RssItem>> =
        httpClient.get(
            AnimeModuleResource.Qb.Rss.Articles(
                rssId = rssId,
                isRead = isRead,
                page = page,
                size = size
            )
        ).body()

    suspend fun addQbRssFolder(path: String): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Folder()) {
            setBody(buildJsonObject {
                put("path", path)
            })
        }.body()
    

    suspend fun addQbRssFeed(url: String, path: String? = null): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Feed()) {
            setBody(buildJsonObject {
                put("url", url)
                path?.let { put("path", it) }
            })
        }.body()
    

    suspend fun getQbRssItems(withData: Boolean = false): Result<Map<String, Any>> =
        httpClient.get(AnimeModuleResource.Qb.Rss.Item(withData = withData)).body()
    

    suspend fun removeQbRssItem(path: String): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Item.Remove()) {
            setBody(buildJsonObject {
                put("path", path)
            })
        }.body()
    

    suspend fun moveQbRssItem(itemPath: String, destPath: String): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Item.Move()) {
            setBody(buildJsonObject {
                put("itemPath", itemPath)
                put("destPath", destPath)
            })
        }.body()
    

    suspend fun refreshQbRssItem(itemPath: String? = null): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Item.Refresh()) {
            setBody(buildJsonObject {
                itemPath?.let { put("itemPath", it) }
            })
        }.body()
    

    suspend fun getQbRssRules(): Result<Map<String, Any>> =
        httpClient.get(AnimeModuleResource.Qb.Rss.Rule()).body()
    

    suspend fun setQbRssRule(ruleName: String, ruleDef: Map<String, Any>): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Rule()) {
            setBody(buildJsonObject {
                put("ruleName", ruleName)
                put("ruleDef", kotlinx.serialization.json.Json.encodeToJsonElement(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    kotlinx.serialization.json.JsonObject(ruleDef.mapValues {
                        kotlinx.serialization.json.JsonPrimitive(it.value.toString())
                    })
                ))
            })
        }.body()
    

    suspend fun renameQbRssRule(ruleName: String, newRuleName: String): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Rule.Rename()) {
            setBody(buildJsonObject {
                put("ruleName", ruleName)
                put("newRuleName", newRuleName)
            })
        }.body()
    

    suspend fun removeQbRssRule(ruleName: String): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.Rule.Remove()) {
            setBody(buildJsonObject {
                put("ruleName", ruleName)
            })
        }.body()
    

    suspend fun getQbRssMatchingArticles(ruleName: String): Result<Map<String, List<String>>> =
        httpClient.get(AnimeModuleResource.Qb.Rss.Rule.MatchingArticles(ruleName = ruleName)).body()
    

    suspend fun markQbRssAsRead(itemPath: String, articleId: String? = null): Result<Unit> =
        httpClient.post(AnimeModuleResource.Qb.Rss.MarkAsRead()) {
            setBody(buildJsonObject {
                put("itemPath", itemPath)
                articleId?.let { put("articleId", it) }
            })
        }.body()
    
}