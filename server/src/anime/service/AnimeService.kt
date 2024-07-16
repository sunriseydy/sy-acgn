package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.db.AnimeSeasonTable.season
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.repository.AnimeRepository
import dev.sunriseydy.acgn.anime.tools.AnimeCacheTool

/**
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
class AnimeService(val animeRepository: AnimeRepository = AnimeRepository()) {
    suspend fun getAnimeNameAndId(name: String? = null) = AnimeCacheTool.getAnimeIdAndNameMap(name)
    suspend fun getAnimeSeasonByAnimeId(animeId: ULong) = animeRepository.selectAnimeSeasonByAnimeId(animeId)

    suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason {
        // 只能新增数据
        check(season.id == ULong.MIN_VALUE) { "只能新增数据" }
        var anime = season.anime
        var newSeason = season
        if (newSeason.animeId == ULong.MIN_VALUE) {
            // 动画系列也要新增
            checkNotNull(anime) { "新增动画时的动画数据为空" }
            anime = animeRepository.insertAnime(anime)
            this.refreshAnimeCache()
            newSeason = newSeason.copy(animeId = anime.id)
        }
        check(newSeason.animeId != ULong.MIN_VALUE) { "必须关联动画" }
        return animeRepository.insertAnimeSeason(newSeason)
    }

    suspend fun refreshAnimeCache() = AnimeCacheTool.refreshAnimeMap(animeRepository.selectAllAnime())

    suspend fun removeAnimeById(id: ULong) {
        animeRepository.deleteAnimeById(id)
        animeRepository.deleteAnimeSeasonByAnimeId(id)
        animeRepository.deleteAnimeEpisodeByAnimeId(id)
        this.refreshAnimeCache()
    }

    suspend fun removeAnimeSeasonById(id: ULong) {
        animeRepository.deleteAnimeSeasonById(id)
        animeRepository.deleteAnimeEpisodeBySeasonId(id)
    }

    suspend fun removeAnimeEpisodeById(id: ULong) {
        animeRepository.deleteAnimeEpisodeById(id)
    }
}