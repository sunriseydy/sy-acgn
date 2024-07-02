package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.db.RssDAO
import dev.sunriseydy.acgn.anime.db.RssItemDAO
import dev.sunriseydy.acgn.anime.db.RssItemTable
import dev.sunriseydy.acgn.anime.db.RssTable
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.tools.RssTool
import dev.sunriseydy.acgn.plugins.suspendTransaction
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
    suspend fun getRssList(): List<Rss> = suspendTransaction {
        RssDAO.all().map(RssDAO::toDTO)
    }

    suspend fun createRss(link: String): Rss {
        // 1. 从 url 中获取 rss
        var rss = RssTool().use{ it.fetchRss(link) }
        var rssItems = rss.items
        // 2. 插入 rss
        rss = this.insertRss(rss)
        // 3. 插入 rss item
        rssItems?.forEach { rssItem ->
            rssItem.rssId = rss.id!!
            this.insertRssItem(rssItem)
        }
        return rss
    }

    suspend fun saveRss(rss: Rss) {
        rss.id?.let { this.updateRss(rss) }
    }

    suspend fun removeRss(id: ULong) {
        this.deleteRss(id)
        this.deleteRssItemByRssId(id)
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
        RssDAO.findSingleByAndUpdate(
            (RssTable.id eq rss.id!!) and
                    (RssTable.version eq rss.version!!)
        ) {
            it.title = rss.title
            it.description = rss.description
            it.ttl = rss.ttl
            it.version = rss.version!! + 1
        }?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun deleteRss(id: ULong): Unit = suspendTransaction {
        RssDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }

    suspend fun selectRssItemByRssIdOrIsRead(rssId: ULong?, isRead: Boolean?) = suspendTransaction {
        RssItemDAO.find {
            (rssId?.let { RssItemTable.rssId eq it } ?: Op.TRUE) and
                    (isRead?.let { RssItemTable.isRead eq it } ?: Op.TRUE)
        }.sortedByDescending { it.publishedAt }
            .map(RssItemDAO::toDTO)
    }

    suspend fun insertRssItem(rssItem: RssItem) = suspendTransaction {
        RssItemDAO.new {
            this.rssId = rssItem.rssId!!
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