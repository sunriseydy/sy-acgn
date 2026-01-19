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
}