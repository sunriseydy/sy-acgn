package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.repository.RssRepository

/**
 * @author SunriseYDY
 * @date 2024-06-29 00:50
 */
class RssService(
    private val rssRepository: RssRepository,
) {
    suspend fun getRssList(): List<Rss> = rssRepository.queryAll()

    suspend fun getRss(id: ULong): Rss = rssRepository.queryById(id)

    suspend fun createRss(rss: Rss): Rss = rssRepository.insert(rss)

    suspend fun updateRss(rss: Rss): Rss = rssRepository.update(rss)

    suspend fun deleteRss(id: ULong): Unit = rssRepository.delete(id)
}