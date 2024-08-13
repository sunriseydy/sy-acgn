package dev.sunriseydy.acgn.server.anime.service

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.server.anime.repository.RssRepository
import dev.sunriseydy.acgn.server.anime.tools.RssTool
import kotlinx.datetime.Clock
import java.util.UUID

/**
 * @author SunriseYDY
 * @date 2024-06-29 00:50
 */
class RssService(val rssRepository: RssRepository = RssRepository()) {

    suspend fun getAllRss() = rssRepository.selectAllRss()

    suspend fun createRss(link: String): Rss {
        // 1. 从 url 中获取 rss
        var rss = this.fetchRssFromLink(link)
        var rssItems = rss.items
        // 2. 插入 rss
        rss = rssRepository.insertRss(rss)
        // 3. 插入 rss item
        rssItems.forEach { rssItem ->
            rssRepository.insertRssItem(rssItem.copy(rssId = rss.id))
        }
        return rss
    }

    suspend fun saveRss(rss: Rss) {
        rssRepository.updateRss(rss)
    }

    suspend fun fetchRssFromLink(link: String) = RssTool().fetchRss(link)

    suspend fun fetchRss(rssId: ULong?) = rssId?.let { this.fetchRssByRssId(rssId) } ?: this.fetchAllRss()

    suspend fun fetchAllRss() {
        rssRepository.selectAllRss().forEach {
            this.fetchRssByRss(it)
        }
    }

    suspend fun fetchRssByRssId(rssId: ULong) {
        rssRepository.selectRssById(rssId).let {
            this.fetchRssByRss(it)
        }
    }

    suspend fun fetchRssByRss(rss: Rss) = this.fetchRssFromLink(rss.link).items.forEach {
        // 先查询是否已存在，不存在则插入
        rssRepository.selectRssItemByRssIdAndGuid(rss.id, it.guid) ?: rssRepository.insertRssItem(it)
    }.also {
        // 更新 rss 的 lastFetchAt
        rssRepository.updateRss(rss.copy(lastFetchAt = Clock.System.now()))
    }

    suspend fun removeRss(id: ULong) {
        rssRepository.deleteRss(id)
        rssRepository.deleteRssItemByRssId(id)
    }

    suspend fun getRssItemByRssIdOrIsRead(
        rssId: ULong?,
        isRead: Boolean?,
        page: Long? = null,
        size: Int? = null,
    ) = rssRepository.selectRssItemByRssIdOrIsRead(rssId, isRead, page, size)

    suspend fun markRssItemReadByIdOrRssId(
        id: UUID?,
        rssId: ULong?
    ) = rssRepository.updateRssItemReadByIdOrRssId(id, rssId)
}