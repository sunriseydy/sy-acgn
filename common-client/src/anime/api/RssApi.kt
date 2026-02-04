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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * @author SunriseYDY
 * @date 2024-07-23 11:33
 */
class RssApi internal constructor(private val httpClient: HttpClient) {
    fun getAllRss(): Result<List<Rss>> = runBlocking {
        httpClient.get(AnimeModuleResource.Rss()).body()
    }

    fun saveRss(id: ULong, rss: Rss): Result<Unit> = runBlocking {
        httpClient.put(AnimeModuleResource.Rss.Id(id = id)) {
            setBody(rss)
        }.body()
    }

    fun fetchRss(rssId: ULong): Result<Unit> = runBlocking {
        httpClient.put(AnimeModuleResource.Rss.Fetch(rssId = rssId)).body()
    }

    fun createRss(link: String): Result<Rss> = runBlocking {
        httpClient.post(AnimeModuleResource.Rss()) {
            setBody(buildJsonObject {
                put("link", link)
            })
        }.body()
    }

    fun deleteRss(id: ULong): Result<Unit> = runBlocking {
        httpClient.delete(AnimeModuleResource.Rss.Id(id = id)).body()
    }

    fun getRssItemByRssIdOrIsRead(
        rssId: ULong?,
        isRead: Boolean?,
        page: Long,
        size: Int,
    ): Result<List<RssItem>> = runBlocking {
        httpClient.get(
            AnimeModuleResource.Rss.Item(
                rssId = rssId,
                isRead = isRead,
                page = page,
                size = size
            )
        ).body()
    }

    fun markRssItemReadByIdOrRssId(
        id: String? = null,
        rssId: ULong? = null
    ): Result<Int> = runBlocking {
        httpClient.put(
            AnimeModuleResource.Rss.Item.Read(
                id = id,
                rssId = rssId
            )
        ).body()
    }

    fun addQbTorrent(torrentAdd: TorrentAdd): Result<String> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Torrent()) {
            setBody(torrentAdd)
        }.body()
    }

    // qBittorrent RSS API

    fun getQbRssList(withData: Boolean = false): Result<List<Rss>> = runBlocking {
        httpClient.get(AnimeModuleResource.Qb.Rss.List(withData = withData)).body()
    }

    fun getQbRssArticles(
        rssId: ULong?,
        isRead: Boolean?,
        page: Long,
        size: Int,
    ): Result<List<RssItem>> = runBlocking {
        httpClient.get(
            AnimeModuleResource.Qb.Rss.Articles(
                rssId = rssId,
                isRead = isRead,
                page = page,
                size = size
            )
        ).body()
    }

    fun addQbRssFolder(path: String): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.Folder()) {
            setBody(buildJsonObject {
                put("path", path)
            })
        }.body()
    }

    fun addQbRssFeed(url: String, path: String? = null): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.Feed()) {
            setBody(buildJsonObject {
                put("url", url)
                path?.let { put("path", it) }
            })
        }.body()
    }

    fun getQbRssItems(withData: Boolean = false): Result<Map<String, Any>> = runBlocking {
        httpClient.get(AnimeModuleResource.Qb.Rss.Item(withData = withData)).body()
    }

    fun removeQbRssItem(path: String): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.Item.Remove()) {
            setBody(buildJsonObject {
                put("path", path)
            })
        }.body()
    }

    fun moveQbRssItem(itemPath: String, destPath: String): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.Item.Move()) {
            setBody(buildJsonObject {
                put("itemPath", itemPath)
                put("destPath", destPath)
            })
        }.body()
    }

    fun refreshQbRssItem(itemPath: String? = null): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.Item.Refresh()) {
            setBody(buildJsonObject {
                itemPath?.let { put("itemPath", it) }
            })
        }.body()
    }

    fun getQbRssRules(): Result<Map<String, Any>> = runBlocking {
        httpClient.get(AnimeModuleResource.Qb.Rss.Rule()).body()
    }

    fun setQbRssRule(ruleName: String, ruleDef: Map<String, Any>): Result<Unit> = runBlocking {
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
    }

    fun renameQbRssRule(ruleName: String, newRuleName: String): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.Rule.Rename()) {
            setBody(buildJsonObject {
                put("ruleName", ruleName)
                put("newRuleName", newRuleName)
            })
        }.body()
    }

    fun removeQbRssRule(ruleName: String): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.Rule.Remove()) {
            setBody(buildJsonObject {
                put("ruleName", ruleName)
            })
        }.body()
    }

    fun getQbRssMatchingArticles(ruleName: String): Result<Map<String, List<String>>> = runBlocking {
        httpClient.get(AnimeModuleResource.Qb.Rss.Rule.MatchingArticles(ruleName = ruleName)).body()
    }

    fun markQbRssAsRead(itemPath: String, articleId: String? = null): Result<Unit> = runBlocking {
        httpClient.post(AnimeModuleResource.Qb.Rss.MarkAsRead()) {
            setBody(buildJsonObject {
                put("itemPath", itemPath)
                articleId?.let { put("articleId", it) }
            })
        }.body()
    }
}