package dev.sunriseydy.acgn.anime.repository.impl

import dev.sunriseydy.acgn.anime.db.RssDAO
import dev.sunriseydy.acgn.anime.db.RssTable
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.repository.RssRepository
import dev.sunriseydy.acgn.plugins.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

/**
 * @author SunriseYDY
 * @date 2024-06-28 21:26
 */
class RssRepositoryImpl : RssRepository {
    override suspend fun queryAll(): List<Rss> = suspendTransaction {
        RssDAO.all().map(RssDAO::toDTO)
    }

    override suspend fun queryById(id: ULong): Rss = suspendTransaction {
        RssDAO.findById(id)?.toDTO()
            ?: throw NoSuchElementException()
    }

    override suspend fun insert(rss: Rss): Rss = suspendTransaction {
        RssDAO.new {
            this.link = rss.link
            this.title = rss.title
            this.description = rss.description
            this.ttl = rss.ttl
            this.lastFetchAt = rss.lastFetchAt
        }.toDTO()
    }

    override suspend fun update(rss: Rss): Rss = suspendTransaction {
        RssDAO.findSingleByAndUpdate(
            (RssTable.id eq rss.id) and
                    (RssTable.version eq rss.version)
        ) {
            it.link = rss.link
            it.title = rss.title
            it.description = rss.description
            it.ttl = rss.ttl
            it.lastFetchAt = rss.lastFetchAt
            it.version = rss.version + 1
        }?.toDTO() ?: throw NoSuchElementException()
    }

    override suspend fun delete(id: ULong): Unit = suspendTransaction {
        RssDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }
}