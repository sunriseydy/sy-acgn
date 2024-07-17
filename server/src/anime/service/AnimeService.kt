package dev.sunriseydy.acgn.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.repository.AnimeRepository
import dev.sunriseydy.acgn.anime.tools.AnimeCacheTool
import dev.sunriseydy.acgn.common.repository.AdditionalInfoRepository

/**
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
class AnimeService(
    val animeRepository: AnimeRepository = AnimeRepository(),
    val additionalInfoRepository: AdditionalInfoRepository = AdditionalInfoRepository()
) {
    fun getAnimeNameAndId(name: String? = null) = AnimeCacheTool.getAnimeIdAndNameMap(name)
    suspend fun getAnimeSeasonByAnimeId(animeId: ULong) = animeRepository.selectAnimeSeasonByAnimeId(animeId)

    suspend fun createAnime(anime: Anime): Anime =
        check(anime.id == ULong.MIN_VALUE) { "只能新增数据" }
            .let {
                animeRepository.insertAnime(anime)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(anime.additions, it.id)
                    }
            }

    suspend fun createAnimeSeason(season: AnimeSeason): AnimeSeason =
        // 只能新增数据
        check(season.id == ULong.MIN_VALUE) { "只能新增数据" }
            .let {
                animeRepository.insertAnimeSeason(season)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(season.additions, it.id)
                    }
            }

    suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason =
        // 只能新增数据
        check(season.id == ULong.MIN_VALUE) { "只能新增数据" }
            .let {
                season.copy(animeId =
                if (season.animeId == ULong.MIN_VALUE) {
                    // 动画系列也要新增
                    var anime = season.anime
                    checkNotNull(anime) { "新增动画时的动画数据为空" }
                    this.createAnime(anime).also {
                        this.refreshAnimeCache()
                    }.id
                } else {
                    // 动画系列已存在
                    season.animeId
                }
                ).let {
                    check(it.animeId != ULong.MIN_VALUE) { "必须关联动画" }
                    this.createAnimeSeason(it)
                }
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