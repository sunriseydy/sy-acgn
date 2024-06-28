package dev.sunriseydy.acgn.anime.repository

import dev.sunriseydy.acgn.anime.dto.Rss

/**
 * @author SunriseYDY
 * @date 2024-06-28 21:06
 */
interface RssRepository {
    suspend fun queryAll(): List<Rss>
    suspend fun queryById(id: ULong): Rss
    suspend fun insert(rss: Rss): Rss
    suspend fun update(rss: Rss): Rss
    suspend fun delete(id: ULong)
}