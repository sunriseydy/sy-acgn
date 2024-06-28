package dev.sunriseydy.acgn.anime.repository.impl

import dev.sunriseydy.acgn.anime.db.RssItemDAO
import dev.sunriseydy.acgn.anime.db.RssItemTable
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.repository.RssItemRepository
import dev.sunriseydy.acgn.plugins.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-06-28 21:27
 */
class RssItemRepositoryImpl : RssItemRepository {
    override suspend fun queryAll(): List<RssItem> = suspendTransaction {
        RssItemDAO.all().sortedByDescending{ it.createdAt }
    }.map(RssItemDAO::toDTO)

    override suspend fun queryByRssId(rssId: Long): List<RssItem> = suspendTransaction {
        RssItemDAO.find {
            RssItemTable.rssId eq rssId
        }.sortedByDescending{ it.createdAt }
    }.map(RssItemDAO::toDTO)

    override suspend fun queryByRssIdAndIsRead(rssId: Long?, isRead: Boolean): List<RssItem> = suspendTransaction {
        RssItemDAO.find {
            (rssId?.let { RssItemTable.rssId eq it } ?: Op.TRUE) and
                    (RssItemTable.isRead eq isRead)
        }.sortedByDescending{ it.createdAt }
    }.map(RssItemDAO::toDTO)

    override suspend fun queryById(id: UUID): RssItem = suspendTransaction {
        RssItemDAO.findById(id)
    }?.toDTO() ?: throw NoSuchElementException()

    override suspend fun insert(rssItem: RssItem): RssItem = suspendTransaction {
        RssItemDAO.new {
            this.rssId = rssItem.rssId
            this.link = rssItem.link
            this.title = rssItem.title
            this.description = rssItem.description
            this.content = rssItem.content
            this.torrent = rssItem.torrent
            this.isRead = rssItem.isRead
        }
    }.toDTO()

    override suspend fun update(rssItem: RssItem): RssItem = suspendTransaction {
        RssItemDAO.findSingleByAndUpdate(
            (RssItemTable.id eq UUID.fromString(rssItem.uuid)) and
                    (RssItemTable.version eq rssItem.version)
        ) {
            it.rssId = rssItem.rssId
            it.link = rssItem.link
            it.title = rssItem.title
            it.description = rssItem.description
            it.content = rssItem.content
            it.torrent = rssItem.torrent
            it.isRead = rssItem.isRead
            it.version = rssItem.version + 1
       }
    }?.toDTO() ?: throw NoSuchElementException()

    override suspend fun delete(id: UUID): Unit = suspendTransaction {
        RssItemDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }
}