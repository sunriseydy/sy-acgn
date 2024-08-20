package dev.sunriseydy.acgn.server.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.server.anime.repository.AnimeRepository
import dev.sunriseydy.acgn.server.anime.tools.AnimeCacheTool
import dev.sunriseydy.acgn.server.anime.tools.FileTool
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository

/**
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
class AnimeService(
    val animeRepository: AnimeRepository = AnimeRepository(),
    val additionalInfoRepository: AdditionalInfoRepository = AdditionalInfoRepository()
) {
    suspend fun getAllAnimeWithAdditionFromDB(): List<Anime> {
        return animeRepository.selectAllAnime().map {
            it.copy(
                additions = additionalInfoRepository.selectAdditionalInfos(
                    AnimeAssociatedType.ANIME.localizationKey,
                    it.id
                )
            )
        }
    }

    suspend fun searchAnimeByName(name: String?): List<Anime> {
        if (AnimeCacheTool.isEmpty()) {
            this.refreshAnimeCache()
        }
        return if (name.isNullOrBlank()) {
            AnimeCacheTool.getAnimeList()
        } else {
            AnimeCacheTool.getAnimeList().filter { it.name.contains(name, ignoreCase = true) }
        }
    }

    suspend fun getAllAnimeWithAdditionFromCache(): List<Anime> {
        if (AnimeCacheTool.isEmpty()) {
            this.refreshAnimeCache()
        }
        return AnimeCacheTool.getAnimeList()
    }

    suspend fun getAnimeById(id: ULong): Anime? {
        if (AnimeCacheTool.isEmpty()) {
            this.refreshAnimeCache()
        }
        return AnimeCacheTool.getAnimeById(id)
    }

    suspend fun getAnimeSeasonsWithAdditionAndAnimeById(id: ULong): AnimeSeason {
        return animeRepository.selectAnimeSeasonById(id).let {
            it.copy(
                additions = additionalInfoRepository.selectAdditionalInfos(
                    AnimeAssociatedType.ANIME_SEASON.localizationKey,
                    it.id
                ),
                anime = this.getAnimeById(it.animeId)
            )
        }
    }

    suspend fun getAnimeSeasonsWithAdditionByAnimeId(animeId: ULong) =
        animeRepository.selectAnimeSeasonByAnimeId(animeId)
            .map {
                it.copy(
                    additions = additionalInfoRepository.selectAdditionalInfos(
                        AnimeAssociatedType.ANIME_SEASON.localizationKey,
                        it.id
                    ),
                )
            }

    suspend fun getAnimeSeasonYears() = animeRepository.selectAnimeSeasonYears()

    suspend fun getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(
        year: Int,
        monthType: AnimeMonthType? = null
    ): List<AnimeSeason> {
        return animeRepository.selectAnimeSeasonsByYearAndMonth(year, monthType?.months)
            .map {
                it.copy(
                    additions = additionalInfoRepository.selectAdditionalInfos(
                        AnimeAssociatedType.ANIME_SEASON.localizationKey,
                        it.id
                    ),
                    anime = this.getAnimeById(it.animeId)
                )
            }
    }

    suspend fun getAnimeSeasonSectionMap(): MutableMap<String, List<AnimeSeason>> {
        val sectionMap: MutableMap<String, List<AnimeSeason>> = mutableMapOf()
        getAnimeSeasonYears().let { years ->
            years.forEach { year ->
                AnimeMonthType.entries.forEach { month ->
                    getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(year, month).let { seasons ->
                        if (seasons.isNotEmpty()) {
                            sectionMap["$year - ${month.localization}"] = seasons
                        }
                    }
                }
            }
        }
        return sectionMap
    }

    suspend fun createAnime(anime: Anime): Anime =
        check(anime.id == ULong.MIN_VALUE) { "只能新增数据" }
            .let {
                animeRepository.insertAnime(anime)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(anime.additions, it.id)
                        this.refreshAnimeCache()
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

    suspend fun updateAnime(anime: Anime): Anime =
        check(anime.id != ULong.MIN_VALUE) { "只能更新数据" }
            .let {
                animeRepository.updateAnime(anime)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(anime.additions, it.id)
                        this.refreshAnimeCache()
                    }
            }

    suspend fun updateAnimeSeason(season: AnimeSeason): AnimeSeason =
        check(season.id != ULong.MIN_VALUE) { "只能更新数据" }
            .let {
                animeRepository.updateAnimeSeason(season)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(season.additions, it.id)
                    }
            }

    suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason =
        season.copy(animeId =
        if (season.animeId == ULong.MIN_VALUE) {
            // 新增动画系列
            var anime = season.anime
            checkNotNull(anime) { "新增动画时的动画数据为空" }
            this.createAnime(anime).id
        } else {
            // 动画系列已存在
            season.animeId
        }
        ).let {
            check(it.animeId != ULong.MIN_VALUE) { "必须关联动画" }
            (if (it.id == ULong.MIN_VALUE) {
                this.createAnimeSeason(it)
            } else {
                this.updateAnimeSeason(it)
            }).let {
                this.getAnimeSeasonsWithAdditionAndAnimeById(it.id)
            }
        }

    suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile) =
        this.getAnimeSeasonsWithAdditionAndAnimeById(animeSeasonFile.id)
            .let {
                FileTool().handleAnimeSeasonFile(it, animeSeasonFile)
            }

    suspend fun refreshAnimeCache() = AnimeCacheTool.refreshAnimeMap(this.getAllAnimeWithAdditionFromDB())

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