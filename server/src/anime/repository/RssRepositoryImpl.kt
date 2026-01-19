package dev.sunriseydy.acgn.server.anime.repository

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.server.anime.db.RssDAO
import dev.sunriseydy.acgn.server.anime.db.RssItemDAO
import dev.sunriseydy.acgn.server.anime.db.RssItemTable
import dev.sunriseydy.acgn.server.anime.db.RssTable
import dev.sunriseydy.acgn.server.base.plugins.paging
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-07-05 09:35
 */
class RssRepositoryImpl : RssRepository {
    override suspend fun selectAllRss(): List<Rss> = suspendTransaction {
        RssDAO.all().orderBy(Pair(RssTable.id, SortOrder.ASC)).map(RssDAO::toDTO)
    }

    override suspend fun selectRssById(id: ULong) = suspendTransaction {
        RssDAO.findById(id)?.toDTO() ?: throw NoSuchElementException()
    }

    override suspend fun insertRss(rss: Rss): Rss = suspendTransaction {
        RssDAO.new {
            this.link = rss.link
            this.title = rss.title
            this.description = rss.description
            this.ttl = rss.ttl
            this.lastFetchAt = rss.lastFetchAt
        }.toDTO()
    }

    override suspend fun updateRss(rss: Rss) = suspendTransaction {
        RssDAO.findByIdAndUpdate(rss.id) {
            it.title = rss.title
            it.description = rss.description
            it.ttl = rss.ttl
            it.lastFetchAt = rss.lastFetchAt
        }?.toDTO() ?: throw NoSuchElementException()
    }

    override suspend fun deleteRss(id: ULong): Unit = suspendTransaction {
        RssDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }

    override suspend fun selectRssItemByRssIdOrIsRead(rssId: ULong?, isRead: Boolean?, page: Long?, size: Int?) =
        suspendTransaction {
            RssItemDAO.find {
                (rssId?.let { RssItemTable.rssId eq it } ?: Op.TRUE) and
                        (isRead?.let { RssItemTable.isRead eq it } ?: Op.TRUE)
            }.paging(page, size).sortedByDescending { it.publishedAt }.map(RssItemDAO::toDTO)
        }

    override suspend fun selectRssItemByRssIdAndGuid(rssId: ULong, guid: String) = suspendTransaction {
        RssItemDAO.find {
            (RssItemTable.rssId eq rssId) and
                    (RssItemTable.guid eq guid)
        }.firstOrNull()?.toDTO()
    }

    override suspend fun insertRssItem(rssItem: RssItem): UUID = suspendTransaction {
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
        }.id.value
    }

    override suspend fun updateRssItemReadByIdOrRssId(id: UUID?, rssId: ULong?): Unit = suspendTransaction {
        RssItemTable.update({
            (id?.let { RssItemTable.id eq it } ?: Op.TRUE) and
                    (rssId?.let { RssItemTable.rssId eq it } ?: Op.TRUE) and
                    (RssItemTable.isRead eq false)
        }) {
            it[isRead] = true
        }
    }

    override suspend fun deleteRssItemByRssId(rssId: ULong): Unit = suspendTransaction {
        RssItemTable.deleteWhere { RssItemTable.rssId eq rssId }
    }

    override suspend fun getUnreadRssItemCount(rssId: ULong) = suspendTransaction {
        RssItemDAO.count(RssItemTable.rssId eq rssId and (RssItemTable.isRead eq false))
    }
}
