package dev.sunriseydy.acgn.server.anime.repository

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-07-05 09:35
 */
interface RssRepository {
    suspend fun selectAllRss(): List<Rss>
    suspend fun selectRssById(id: ULong): Rss
    suspend fun insertRss(rss: Rss): Rss
    suspend fun updateRss(rss: Rss): Rss
    suspend fun deleteRss(id: ULong)
    suspend fun selectRssItemByRssIdOrIsRead(rssId: ULong?, isRead: Boolean?, page: Long? = null, size: Int? = null): List<RssItem>
    suspend fun selectRssItemByRssIdAndGuid(rssId: ULong, guid: String): RssItem?
    suspend fun insertRssItem(rssItem: RssItem): UUID
    suspend fun updateRssItemReadByIdOrRssId(id: UUID?, rssId: ULong?)
    suspend fun deleteRssItemByRssId(rssId: ULong)
    suspend fun getUnreadRssItemCount(rssId: ULong): Long
}