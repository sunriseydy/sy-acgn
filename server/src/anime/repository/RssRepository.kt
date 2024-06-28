package dev.sunriseydy.acgn.anime.repository

import dev.sunriseydy.acgn.anime.dto.Rss

/**
 * @author SunriseYDY
 * @date 2024-06-28 21:06
 */
interface RssRepository {
    fun queryAll(): List<Rss>
    fun queryById(id: ULong): Rss?
    fun insert(rss: Rss): Rss
    fun update(rss: Rss): Rss
    fun delete(id: ULong): Boolean
}