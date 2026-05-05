package dev.sunriseydy.acgn.server.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType

/**
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
interface AnimeService {
    suspend fun searchAnimeByName(name: String?): List<Anime>
    suspend fun getAnimeSeasonSectionMap(name: String? = null): MutableMap<String, List<AnimeSeason>>
    suspend fun createAnimeSeason(season: AnimeSeason): AnimeSeason
    suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason
    suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile)
    suspend fun removeAnimeSeasonById(id: ULong)
    suspend fun removeAnimeEpisodeById(id: ULong)
    suspend fun refreshCache()
}