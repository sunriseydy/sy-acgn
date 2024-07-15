package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.repository.AnimeRepository

/**
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
class AnimeService(val animeRepository: AnimeRepository = AnimeRepository()) {
    suspend fun getAnimeNameAndId() = animeRepository.selectAnimeNameAndId()
}