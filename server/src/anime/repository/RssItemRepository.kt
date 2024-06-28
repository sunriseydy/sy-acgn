package dev.sunriseydy.acgn.anime.repository

import dev.sunriseydy.acgn.anime.dto.RssItem
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-06-28 21:10
 */
interface RssItemRepository {
    suspend fun queryAll(): List<RssItem>
    suspend fun queryByRssId(rssId: Long): List<RssItem>
    suspend fun queryByRssIdAndIsRead(rssId: Long?, isRead: Boolean?): List<RssItem>
    suspend fun queryById(id: UUID): RssItem
    suspend fun insert(rssItem: RssItem): RssItem
    suspend fun update(rssItem: RssItem): RssItem
    suspend fun delete(id: UUID)
}