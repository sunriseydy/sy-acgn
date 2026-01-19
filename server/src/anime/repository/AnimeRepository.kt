package dev.sunriseydy.acgn.server.anime.repository

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason

/**
 * @author SunriseYDY
 * @date 2024-07-05 10:57
 */
interface AnimeRepository {
    suspend fun selectAllAnime(): List<Anime>
    suspend fun selectAnimeById(id: ULong): Anime
    suspend fun selectAnimeSeasonById(id: ULong): AnimeSeason
    suspend fun selectAnimeSeasonByAnimeId(animeId: ULong): List<AnimeSeason>
    suspend fun selectAnimeSeasonYears(): List<Int>
    suspend fun selectAnimeSeasonsByYearAndMonth(year: Int, months: List<Int>?): List<AnimeSeason>
    suspend fun selectAnimeEpisodeById(id: ULong): AnimeEpisode
    suspend fun selectAnimeEpisodeBySeasonId(seasonId: ULong): List<AnimeEpisode>
    suspend fun insertAnime(anime: Anime): Anime
    suspend fun insertAnimeSeason(animeSeason: AnimeSeason): AnimeSeason
    suspend fun insertAnimeEpisode(animeEpisode: AnimeEpisode): AnimeEpisode
    suspend fun updateAnime(anime: Anime): Anime
    suspend fun updateAnimeSeason(animeSeason: AnimeSeason): AnimeSeason
    suspend fun deleteAnimeById(id: ULong)
    suspend fun deleteAnimeSeasonById(id: ULong)
    suspend fun deleteAnimeSeasonByAnimeId(animeId: ULong)
    suspend fun deleteAnimeEpisodeById(id: ULong)
    suspend fun deleteAnimeEpisodeBySeasonId(seasonId: ULong)
    suspend fun deleteAnimeEpisodeByAnimeId(animeId: ULong)
}