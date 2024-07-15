package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.repository.AnimeRepository

/**
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
class AnimeService(val animeRepository: AnimeRepository = AnimeRepository()) {
    suspend fun getAnimeNameAndId() = animeRepository.selectAnimeNameAndId()
    suspend fun getAnimeSeasonByAnimeId(animeId: ULong) = animeRepository.selectAnimeSeasonByAnimeId(animeId)

    suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason {
        if (season.id == ULong.MIN_VALUE) {
            val new = animeRepository.insertAnimeSeason(season)
            val anime = season.anime
            if (anime != null && anime.id == ULong.MIN_VALUE) {
                new.anime = animeRepository.insertAnime(anime)
            }
            return new
        }
        return season
    }

    suspend fun removeAnimeById(id: ULong) {
        animeRepository.deleteAnimeById(id)
        animeRepository.deleteAnimeSeasonByAnimeId(id)
        animeRepository.deleteAnimeEpisodeByAnimeId(id)
    }

    suspend fun removeAnimeSeasonById(id: ULong) {
        animeRepository.deleteAnimeSeasonById(id)
        animeRepository.deleteAnimeEpisodeBySeasonId(id)
    }

    suspend fun removeAnimeEpisodeById(id: ULong) {
        animeRepository.deleteAnimeEpisodeById(id)
    }
}