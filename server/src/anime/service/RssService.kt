package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.db.RssDAO
import dev.sunriseydy.acgn.anime.db.RssItemDAO
import dev.sunriseydy.acgn.anime.db.RssItemTable
import dev.sunriseydy.acgn.anime.db.RssTable
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.plugins.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import java.util.UUID

/**
 * @author SunriseYDY
 * @date 2024-06-29 00:50
 */
class RssService {
    suspend fun getRssList(): List<Rss> = suspendTransaction {
        RssDAO.all().map(RssDAO::toDTO)
    }

    suspend fun getRssById(id: ULong): Rss = suspendTransaction {
        RssDAO.findById(id)?.toDTO()
            ?: throw NoSuchElementException()
    }

    suspend fun createRss(rss: Rss): Rss = suspendTransaction {
        RssDAO.new {
            this.link = rss.link
            this.title = rss.title
            this.description = rss.description
            this.ttl = rss.ttl
            this.lastFetchAt = rss.lastFetchAt
        }.toDTO()
    }

    suspend fun updateRss(rss: Rss): Rss = suspendTransaction {
        RssDAO.findSingleByAndUpdate(
            (RssTable.id eq rss.id!!) and
                    (RssTable.version eq rss.version!!)
        ) {
            it.link = rss.link
            it.title = rss.title
            it.description = rss.description
            it.ttl = rss.ttl
            it.lastFetchAt = rss.lastFetchAt
            it.version = rss.version!! + 1
        }?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun getRssItemList(): List<RssItem> = suspendTransaction {
        RssItemDAO.all().sortedByDescending { it.publishedAt }.map(RssItemDAO::toDTO)
    }

    suspend fun getByRssIdAndIsRead(rssId: Long?, isRead: Boolean?): List<RssItem> = suspendTransaction {
        RssItemDAO.find {
            (rssId?.let { RssItemTable.rssId eq it } ?: Op.TRUE) and
                    (isRead?.let { RssItemTable.isRead eq it } ?: Op.TRUE)
        }.sortedByDescending { it.publishedAt }
            .map(RssItemDAO::toDTO)
    }

    suspend fun getRssItemById(id: UUID): RssItem = suspendTransaction {
        RssItemDAO.findById(id)?.toDTO()
            ?: throw NoSuchElementException()
    }

    suspend fun deleteRss(id: ULong): Unit = suspendTransaction {
        RssDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }

    suspend fun insertRssItem(rssItem: RssItem): RssItem = suspendTransaction {
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
        }.toDTO()
    }

    suspend fun deleteRssItem(id: UUID): Unit = suspendTransaction {
        RssItemDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }
}