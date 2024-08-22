package dev.sunriseydy.acgn.client.anime.api

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.dto.TorrentAdd
import dev.sunriseydy.acgn.client.animeModuleApiEndPoint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * @author SunriseYDY
 * @date 2024-07-23 11:33
 */
class RssApi internal constructor(private val httpClient: HttpClient) {
    fun getAllRss(): Result<List<Rss>> = runBlocking {
        httpClient.get {
            animeRssApiEndPoint()
        }.body()
    }

    fun saveRss(id: ULong, rss: Rss): Result<Unit> = runBlocking {
        httpClient.put {
            animeRssApiEndPoint(id.toString())
            setBody(rss)
        }.body()
    }

    fun fetchRss(rssId: ULong): Result<Unit> = runBlocking {
        httpClient.put {
            animeRssApiEndPoint("fetch")
            parameter("rssId", rssId)
        }.body()
    }

    fun createRss(link: String): Result<Rss> = runBlocking {
        httpClient.post {
            animeRssApiEndPoint()
            setBody(buildJsonObject {
                put("link", link)
            })
        }.body()
    }

    fun deleteRss(id: ULong): Result<Unit> = runBlocking {
        httpClient.delete {
            animeRssApiEndPoint(id.toString())
        }.body()
    }

    fun getRssItemByRssIdOrIsRead(
        rssId: ULong?,
        isRead: Boolean?,
        page: Long,
        size: Int,
    ): Result<List<RssItem>> = runBlocking {
        httpClient.get {
            animeRssApiEndPoint("item")
            parameter("rssId", rssId)
            parameter("isRead", isRead)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    fun markRssItemReadByIdOrRssId(
        id: String? = null,
        rssId: ULong? = null
    ): Result<Int> = runBlocking {
        httpClient.put {
            animeRssApiEndPoint("item", "read")
            parameter("id", id)
            parameter("rssId", rssId)
        }.body()
    }

    fun addQbTorrent(torrentAdd: TorrentAdd): Result<String> = runBlocking {
        httpClient.post {
            animeQbApiEndPoint()
            setBody(torrentAdd)
        }.body()
    }

    private fun HttpRequestBuilder.animeRssApiEndPoint(vararg paths: String) {
        animeModuleApiEndPoint("rss", *paths)
    }

    private fun HttpRequestBuilder.animeQbApiEndPoint(vararg paths: String) {
        animeModuleApiEndPoint("qb", "torrent", *paths)
    }
}