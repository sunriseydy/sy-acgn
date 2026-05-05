package dev.sunriseydy.acgn.server.anime.tools

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason

/**
 * 动漫数据缓存工具
 *
 * 使用 Caffeine 缓存库管理动漫数据的缓存。
 * 包含两个缓存：
 * 1. 详情缓存 (animeById)：按 ID 缓存单个动漫详情
 * 2. 季度详情缓存 (animeSeasonById)：按 ID 缓存单个动漫季度
 * 3. 动漫季度列表缓存 (animeSeasonsByAnimeId)：按动漫 ID 缓存该动漫的所有季度 ID 列表
 *
 * @author SunriseYDY
 * @date 2024-07-16 17:32
 */
object AnimeCacheTool {
    // 缓存单个动漫详情
    private val animeByIdCache: Cache<ULong, Anime> = Caffeine.newBuilder()
        .build()

    // 缓存单个动漫季度
    private val seasonByIdCache: Cache<ULong, AnimeSeason> = Caffeine.newBuilder()
        .build()

    // 缓存动漫及其所有的季度 ID
    private val seasonsByAnimeIdCache: Cache<ULong, MutableList<ULong>> = Caffeine.newBuilder()
        .build()

    fun isAnimeEmpty() = animeByIdCache.asMap().isEmpty()
    
    /**
     * 刷新动漫缓存
     */
    fun refreshAnimeCache(animeList: List<Anime>) {
        animeByIdCache.invalidateAll()
        animeList.forEach { setAnime(it) }
    }

    fun setAnime(anime: Anime): Anime {
        animeByIdCache.put(anime.id, anime)
        return anime
    }

    fun getAnimeList() = animeByIdCache.asMap().values.toList()

    fun getAnimeById(id: ULong): Anime? = animeByIdCache.getIfPresent(id)

    fun removeAnime(id: ULong) {
        animeByIdCache.invalidate(id)
    }

    fun isSeasonEmpty() = seasonByIdCache.asMap().isEmpty()

    fun isAnimeSeasonEmpty(animeId: ULong) = seasonsByAnimeIdCache.getIfPresent(animeId).isNullOrEmpty()

    /**
     * 刷新动漫季度缓存
     */
    fun refreshSeasonCache(animeSeasonList: List<AnimeSeason>) {
        seasonByIdCache.invalidateAll()
        seasonsByAnimeIdCache.invalidateAll()
        animeSeasonList.forEach { setSeason(it) }
    }

    fun setSeason(animeSeason: AnimeSeason): AnimeSeason {
        seasonByIdCache.put(animeSeason.id, animeSeason)
        val list = seasonsByAnimeIdCache.getIfPresent(animeSeason.animeId) ?: mutableListOf()
        if (!list.contains(animeSeason.id)) {
            list.add(animeSeason.id)
            seasonsByAnimeIdCache.put(animeSeason.animeId, list)
        }
        return animeSeason
    }

    fun getSeasons(): List<AnimeSeason> = seasonByIdCache.asMap().values.toList()

    fun getAnimeSeasons(animeId: ULong): List<ULong>? = seasonsByAnimeIdCache.getIfPresent(animeId)

    fun getSeason(id: ULong): AnimeSeason? = seasonByIdCache.getIfPresent(id)

    fun removeSeason(id: ULong) {
        seasonByIdCache.getIfPresent(id)?.let { season ->
            seasonByIdCache.invalidate(id)
            val list = seasonsByAnimeIdCache.getIfPresent(season.animeId)
            if (list != null && list.contains(id)) {
                list.remove(id)
                seasonsByAnimeIdCache.put(season.animeId, list)
            }
        }
    }
}