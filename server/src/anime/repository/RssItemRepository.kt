package dev.sunriseydy.acgn.anime.repository

import dev.sunriseydy.acgn.anime.dto.RssItem
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-06-28 21:10
 */
interface RssItemRepository {
    fun queryAll(): List<RssItem>
    fun queryByRssId(rssId: Long): List<RssItem>
    fun queryByRssIdAndIsRead(rssId: Long, isRead: Boolean): List<RssItem>
    fun queryById(id: UUID): RssItem?
    fun insert(rssItem: RssItem): RssItem
    fun update(rssItem: RssItem): RssItem
    fun delete(id: UUID): Boolean
}