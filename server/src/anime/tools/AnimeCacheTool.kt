package dev.sunriseydy.acgn.server.anime.tools

import dev.sunriseydy.acgn.anime.dto.Anime

/**
 * @author SunriseYDY
 * @date 2024-07-16 17:32
 */
object AnimeCacheTool {
    private var animes: List<Anime> = listOf()

    fun isEmpty() = animes.isEmpty()

    fun refreshAnimeMap(animeList: List<Anime>) {
        animes = animeList
    }

    fun getAnimeList() = animes

    fun getAnimeById(id: ULong): Anime? = animes.find { it.id == id }
}