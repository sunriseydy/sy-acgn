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
    suspend fun getAllAnimeWithAdditionFromDB(): List<Anime>
    suspend fun searchAnimeByName(name: String?): List<Anime>
    suspend fun getAllAnimeWithAdditionFromCache(): List<Anime>
    suspend fun getAnimeById(id: ULong): Anime?
    suspend fun getAnimeSeasonsWithAdditionAndAnimeById(id: ULong): AnimeSeason
    suspend fun getAnimeSeasonsWithAdditionByAnimeId(animeId: ULong): List<AnimeSeason>
    suspend fun getAnimeSeasonYears(): List<Int>
    suspend fun getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(year: Int, monthType: AnimeMonthType? = null): List<AnimeSeason>
    suspend fun getAnimeSeasonSectionMap(): MutableMap<String, List<AnimeSeason>>
    suspend fun createAnime(anime: Anime): Anime
    suspend fun createAnimeSeason(season: AnimeSeason): AnimeSeason
    suspend fun updateAnime(anime: Anime): Anime
    suspend fun updateAnimeSeason(season: AnimeSeason): AnimeSeason
    suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason
    suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile)
    suspend fun refreshAnimeCache()
    suspend fun removeAnimeById(id: ULong)
    suspend fun removeAnimeSeasonById(id: ULong)
    suspend fun removeAnimeEpisodeById(id: ULong)
}