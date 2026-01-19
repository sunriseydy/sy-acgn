package dev.sunriseydy.acgn.server.anime.service

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.server.anime.repository.RssRepository
import dev.sunriseydy.acgn.server.anime.repository.RssRepositoryImpl
import dev.sunriseydy.acgn.server.anime.tools.RssTool
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-06-29 00:50
 */
class RssServiceImpl(val rssRepository: RssRepository = RssRepositoryImpl()) : RssService {

    private val rssCache = mutableMapOf<ULong, Rss>()

    override suspend fun getAllRss() = rssRepository.selectAllRss().also {
        it.forEach { rss ->
            rss.unreadCount = getUnreadRssItemCount(rss.id)
        }
        rssCache.clear()
        rssCache.putAll(it.associateBy { it.id })
    }

    override suspend fun createRss(link: String): Rss {
        // 1. 从 url 中获取 rss
        var rss = this.fetchRssFromLink(link)
        val rssItems = rss.items
        // 2. 插入 rss
        rss = rssRepository.insertRss(rss)
        // 3. 插入 rss item
        rssItems.forEach { rssItem ->
            rssRepository.insertRssItem(rssItem.copy(rssId = rss.id))
        }
        return rss
    }

    override suspend fun saveRss(rss: Rss) {
        rssRepository.updateRss(rss)
    }

    override suspend fun fetchRssFromLink(link: String) = RssTool().fetchRss(link)

    override suspend fun fetchRss(rssId: ULong?) = rssId?.let { this.fetchRssByRssId(rssId) } ?: this.fetchAllRss()

    override suspend fun fetchAllRss() {
        rssRepository.selectAllRss().forEach {
            this.fetchRssByRss(it)
        }
    }

    override suspend fun fetchRssByRssId(rssId: ULong) {
        rssRepository.selectRssById(rssId).let {
            this.fetchRssByRss(it)
        }
    }

    override suspend fun fetchRssByRss(rss: Rss) = this.fetchRssFromLink(rss.link).items.forEach {
        // 先查询是否已存在，不存在则插入
        rssRepository.selectRssItemByRssIdAndGuid(rss.id, it.guid) ?: rssRepository.insertRssItem(it)
    }.also {
        // 更新 rss 的 lastFetchAt
        rssRepository.updateRss(rss.copy(lastFetchAt = OffsetDateTime.now(ZoneOffset.UTC)))
    }

    override suspend fun removeRss(id: ULong) {
        rssRepository.deleteRss(id)
        rssRepository.deleteRssItemByRssId(id)
    }

    override suspend fun getRssItemByRssIdOrIsRead(
        rssId: ULong?,
        isRead: Boolean?,
        page: Long?,
        size: Int?,
    ) = rssRepository.selectRssItemByRssIdOrIsRead(rssId, isRead, page, size).also {
        it.forEach { rssItem ->
            rssItem.rss = rssCache[rssItem.rssId]
        }
    }

    override suspend fun markRssItemReadByIdOrRssId(
        id: UUID?,
        rssId: ULong?
    ) = rssRepository.updateRssItemReadByIdOrRssId(id, rssId)

    override suspend fun getUnreadRssItemCount(rssId: ULong) = rssRepository.getUnreadRssItemCount(rssId)
}
