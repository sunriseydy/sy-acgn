package dev.sunriseydy.acgn.server.anime.tools

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.sunriseydy.acgn.anime.dto.Anime
import java.util.concurrent.TimeUnit

/**
 * @author SunriseYDY
 * @date 2024-07-16 17:32
 */
object AnimeCacheTool {
    private const val ANIME_LIST_KEY = "animeList"
    private const val CACHE_EXPIRE_HOURS = 24L

    private val cache: Cache<String, List<Anime>> = Caffeine.newBuilder()
        .expireAfterWrite(CACHE_EXPIRE_HOURS, TimeUnit.HOURS)
        .maximumSize(100)
        .build()

    private val animeByIdCache: Cache<ULong, Anime> = Caffeine.newBuilder()
        .expireAfterWrite(CACHE_EXPIRE_HOURS, TimeUnit.HOURS)
        .maximumSize(10000)
        .build()

    fun isEmpty() = cache.getIfPresent(ANIME_LIST_KEY)?.isEmpty() ?: true

    fun refreshAnimeMap(animeList: List<Anime>) {
        cache.put(ANIME_LIST_KEY, animeList)
        animeByIdCache.invalidateAll()
        animeList.forEach { animeByIdCache.put(it.id, it) }
    }

    fun getAnimeList() = cache.getIfPresent(ANIME_LIST_KEY) ?: listOf()

    fun getAnimeById(id: ULong): Anime? = animeByIdCache.getIfPresent(id)
}