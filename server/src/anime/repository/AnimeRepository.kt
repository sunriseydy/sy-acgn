package dev.sunriseydy.acgn.anime.repository

import dev.sunriseydy.acgn.anime.db.AnimeTable
import dev.sunriseydy.acgn.plugins.suspendTransaction

/**
 * @author SunriseYDY
 * @date 2024-07-05 10:57
 */
class AnimeRepository {
    suspend fun selectAnimeNameAndId(): Map<String, ULong> = suspendTransaction {
        AnimeTable.select(AnimeTable.name, AnimeTable.id).fold(emptyMap()) { map, anime ->
            map.plus(anime[AnimeTable.name] to anime[AnimeTable.id].value)
        }
    }
}