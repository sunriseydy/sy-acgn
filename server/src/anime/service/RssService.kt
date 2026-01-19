package dev.sunriseydy.acgn.server.anime.service

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-06-29 00:50
 */
interface RssService {
    suspend fun getAllRss(): List<Rss>
    suspend fun createRss(link: String): Rss
    suspend fun saveRss(rss: Rss)
    suspend fun fetchRssFromLink(link: String): Rss
    suspend fun fetchRss(rssId: ULong?): Any
    suspend fun fetchAllRss()
    suspend fun fetchRssByRssId(rssId: ULong)
    suspend fun fetchRssByRss(rss: Rss)
    suspend fun removeRss(id: ULong)
    suspend fun getRssItemByRssIdOrIsRead(rssId: ULong?, isRead: Boolean?, page: Long? = null, size: Int? = null): List<RssItem>
    suspend fun markRssItemReadByIdOrRssId(id: UUID?, rssId: ULong?)
    suspend fun getUnreadRssItemCount(rssId: ULong): Long
}