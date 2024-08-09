package dev.sunriseydy.acgn.server.anime.tools

import dev.sunriseydy.acgn.anime.dto.Anime

/**
 * @author SunriseYDY
 * @date 2024-07-16 17:32
 */
object AnimeCacheTool {
    private val animeMap: MutableMap<ULong, Anime> = mutableMapOf()
    fun refreshAnimeMap(animeList: List<Anime>) {
        animeMap.clear()
        animeList.forEach {
            animeMap[it.id] = it
        }
    }

    fun getAnimeList() = animeMap.values.toList()

    fun getAnimeById(id: ULong): Anime? = animeMap[id]

    fun getAnimeIdAndNameMap(name: String? = null): Map<ULong, String> =
        animeMap.filterValues {
            if (name == null) {
                true
            } else {
                it.name.contains(name)
            }
        }.mapValues { "${it.key}-${it.value.name}" }
}