package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.db.RssDAO
import dev.sunriseydy.acgn.anime.db.RssItemDAO
import dev.sunriseydy.acgn.anime.db.RssItemTable
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.tools.RssTool
import dev.sunriseydy.acgn.plugins.suspendTransaction
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import java.util.UUID

/**
 * @author SunriseYDY
 * @date 2024-06-29 00:50
 */
class RssService {

    suspend fun createRss(link: String): Rss {
        // 1. 从 url 中获取 rss
        var rss = this.fetchRssFromLink(link)
        var rssItems = rss.items
        // 2. 插入 rss
        rss = this.insertRss(rss)
        // 3. 插入 rss item
        rssItems.forEach { rssItem ->
            this.insertRssItem(rssItem.copy(rssId = rss.id))
        }
        return rss
    }

    suspend fun saveRss(rss: Rss) {
        this.updateRss(rss)
    }

    suspend fun fetchRssFromLink(link: String) = RssTool().use { it.fetchRss(link) }

    suspend fun fetchRss(rssId: ULong?) = rssId?.let { this.fetchRssByRssId(rssId) } ?: this.fetchAllRss()

    suspend fun fetchAllRss() {
        this.selectAllRss().forEach {
            this.fetchRssByRss(it)
        }
    }

    suspend fun fetchRssByRssId(rssId: ULong) {
        this.selectRssById(rssId).let {
            this.fetchRssByRss(it)
        }
    }

    suspend fun fetchRssByRss(rss: Rss) = this.fetchRssFromLink(rss.link).items.forEach {
        // 先查询是否已存在，不存在则插入
        selectRssItemByRssIdAndGuid(rss.id, it.guid) ?: this.insertRssItem(it)
    }.also {
        // 更新 rss 的 lastFetchAt
        this.updateRss(rss.copy(lastFetchAt = Clock.System.now()))
    }

    suspend fun removeRss(id: ULong) {
        this.deleteRss(id)
        this.deleteRssItemByRssId(id)
    }

    suspend fun selectAllRss(): List<Rss> = suspendTransaction {
        RssDAO.all().map(RssDAO::toDTO)
    }

    suspend fun selectRssById(id: ULong) = suspendTransaction {
        RssDAO.findById(id)?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun insertRss(rss: Rss): Rss = suspendTransaction {
        RssDAO.new {
            this.link = rss.link
            this.title = rss.title
            this.description = rss.description
            this.ttl = rss.ttl
            this.lastFetchAt = rss.lastFetchAt
        }.toDTO()
    }

    suspend fun updateRss(rss: Rss) = suspendTransaction {
        RssDAO.findByIdAndUpdate(rss.id) {
            it.title = rss.title
            it.description = rss.description
            it.ttl = rss.ttl
            it.lastFetchAt = rss.lastFetchAt
        }?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun deleteRss(id: ULong): Unit = suspendTransaction {
        RssDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }

    suspend fun selectRssItemByRssIdOrIsRead(rssId: ULong?, isRead: Boolean?, page: Long = 0, size: Int = 10) =
        suspendTransaction {
            RssItemDAO.find {
                (rssId?.let { RssItemTable.rssId eq it } ?: Op.TRUE) and
                        (isRead?.let { RssItemTable.isRead eq it } ?: Op.TRUE)
            }.also {
                if (page > 0) {
                    it.limit(size, (page - 1) * size)
                }
            }.sortedByDescending { it.publishedAt }.map(RssItemDAO::toDTO)
        }

    suspend fun selectRssItemByRssIdAndGuid(rssId: ULong, guid: String) = suspendTransaction {
        RssItemDAO.find {
            (RssItemTable.rssId eq rssId) and
                    (RssItemTable.guid eq guid)
        }.firstOrNull()?.toDTO()
    }

    suspend fun insertRssItem(rssItem: RssItem) = suspendTransaction {
        RssItemDAO.new {
            this.rssId = rssItem.rssId
            this.link = rssItem.link
            this.guid = rssItem.guid
            this.title = rssItem.title
            this.description = rssItem.description
            this.content = rssItem.content
            this.torrent = rssItem.torrent
            this.isRead = rssItem.isRead
            this.publishedAt = rssItem.publishedAt
        }.id
    }

    suspend fun updateRssItemReadByIdOrRssId(id: UUID?, rssId: ULong?) = suspendTransaction {
        RssItemTable.update({
            (id?.let { RssItemTable.id eq it } ?: Op.TRUE) and
                    (rssId?.let { RssItemTable.rssId eq it } ?: Op.TRUE) and
                    (RssItemTable.isRead eq false)
        }) {
            it[isRead] = true
        }
    }

    suspend fun deleteRssItemByRssId(rssId: ULong): Unit = suspendTransaction {
        RssItemTable.deleteWhere { RssItemTable.rssId eq rssId }
    }
}