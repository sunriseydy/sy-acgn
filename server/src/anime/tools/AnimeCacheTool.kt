package dev.sunriseydy.acgn.server.anime.tools

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.sunriseydy.acgn.anime.dto.Anime
import java.util.concurrent.TimeUnit

/**
 * 动漫数据缓存工具
 *
 * 使用 Caffeine 缓存库管理动漫数据的缓存。
 * 包含两个缓存：
 * 1. 列表缓存 (animeList)：缓存整个动漫列表
 * 2. 详情缓存 (animeById)：按 ID 缓存单个动漫详情
 *
 * 缓存过期时间为 24 小时。
 *
 * @author SunriseYDY
 * @date 2024-07-16 17:32
 */
object AnimeCacheTool {
    private const val ANIME_LIST_KEY = "animeList"
    private const val CACHE_EXPIRE_HOURS = 24L

    // 缓存所有动漫的列表，最大容量 100
    private val cache: Cache<String, List<Anime>> = Caffeine.newBuilder()
        .expireAfterWrite(CACHE_EXPIRE_HOURS, TimeUnit.HOURS)
        .maximumSize(100)
        .build()

    // 缓存单个动漫详情，最大容量 10000
    private val animeByIdCache: Cache<ULong, Anime> = Caffeine.newBuilder()
        .expireAfterWrite(CACHE_EXPIRE_HOURS, TimeUnit.HOURS)
        .maximumSize(10000)
        .build()

    fun isEmpty() = cache.getIfPresent(ANIME_LIST_KEY)?.isEmpty() ?: true

    /**
     * 刷新缓存
     *
     * 更新列表缓存，并清空后重新填充 ID 缓存。
     */
    fun refreshAnimeMap(animeList: List<Anime>) {
        cache.put(ANIME_LIST_KEY, animeList)
        animeByIdCache.invalidateAll()
        animeList.forEach { animeByIdCache.put(it.id, it) }
    }

    fun getAnimeList() = cache.getIfPresent(ANIME_LIST_KEY) ?: listOf()

    fun getAnimeById(id: ULong): Anime? = animeByIdCache.getIfPresent(id)
}