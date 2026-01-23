package dev.sunriseydy.acgn.server.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.base.enums.Status
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.server.anime.repository.AnimeRepository
import dev.sunriseydy.acgn.server.anime.tools.AnimeCacheTool
import dev.sunriseydy.acgn.server.anime.tools.FileTool
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository

/**
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
class AnimeServiceImpl(
    val animeRepository: AnimeRepository,
    val additionalInfoRepository: AdditionalInfoRepository
) : AnimeService {
    override suspend fun getAllAnimeWithAdditionFromDB(): List<Anime> {
        return animeRepository.selectAllAnime().map {
            it.copy(
                additions = additionalInfoRepository.selectAdditionalInfos(
                    AnimeAssociatedType.ANIME.key,
                    it.id
                )
            )
        }
    }

    override suspend fun searchAnimeByName(name: String?): List<Anime> {
        if (AnimeCacheTool.isEmpty()) {
            this.refreshAnimeCache()
        }
        return if (name.isNullOrBlank()) {
            AnimeCacheTool.getAnimeList()
        } else {
            AnimeCacheTool.getAnimeList().filter { it.name.contains(name, ignoreCase = true) }
        }
    }

    override suspend fun getAllAnimeWithAdditionFromCache(): List<Anime> {
        if (AnimeCacheTool.isEmpty()) {
            this.refreshAnimeCache()
        }
        return AnimeCacheTool.getAnimeList()
    }

    override suspend fun getAnimeById(id: ULong): Anime? {
        if (AnimeCacheTool.isEmpty()) {
            this.refreshAnimeCache()
        }
        return AnimeCacheTool.getAnimeById(id)
    }

    override suspend fun getAnimeSeasonsWithAdditionAndAnimeById(id: ULong): AnimeSeason {
        return animeRepository.selectAnimeSeasonById(id).let {
            it.copy(
                additions = additionalInfoRepository.selectAdditionalInfos(
                    AnimeAssociatedType.ANIME_SEASON.key,
                    it.id
                ),
                anime = this.getAnimeById(it.animeId)
            )
        }
    }

    override suspend fun getAnimeSeasonsWithAdditionByAnimeId(animeId: ULong) =
        animeRepository.selectAnimeSeasonByAnimeId(animeId)
            .map {
                it.copy(
                    additions = additionalInfoRepository.selectAdditionalInfos(
                        AnimeAssociatedType.ANIME_SEASON.key,
                        it.id
                    ),
                )
            }

    override suspend fun getAnimeSeasonYears() = animeRepository.selectAnimeSeasonYears()

    override suspend fun getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(
        year: Int,
        monthType: AnimeMonthType?
    ): List<AnimeSeason> {
        return animeRepository.selectAnimeSeasonsByYearAndMonth(year, monthType?.months)
            .map {
                it.copy(
                    additions = additionalInfoRepository.selectAdditionalInfos(
                        AnimeAssociatedType.ANIME_SEASON.key,
                        it.id
                    ),
                    anime = this.getAnimeById(it.animeId)
                )
            }
    }

    override suspend fun getAnimeSeasonSectionMap(): MutableMap<String, List<AnimeSeason>> {
        val sectionMap: MutableMap<String, List<AnimeSeason>> = mutableMapOf()
        getAnimeSeasonYears().let { years ->
            years.forEach { year ->
                AnimeMonthType.entries.forEach { month ->
                    getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(year, month).let { seasons ->
                        if (seasons.isNotEmpty()) {
                            sectionMap["$year - ${month.meaning}"] = seasons
                        }
                    }
                }
            }
        }
        return sectionMap
    }

    override suspend fun createAnime(anime: Anime): Anime =
        check(anime.id == ULong.MIN_VALUE) { "只能新增数据" }
            .let {
                animeRepository.insertAnime(anime)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(anime.additions, it.id)
                        this.refreshAnimeCache()
                    }
            }

    override suspend fun createAnimeSeason(season: AnimeSeason): AnimeSeason =
        // 只能新增数据
        check(season.id == ULong.MIN_VALUE) { "只能新增数据" }
            .let {
                animeRepository.insertAnimeSeason(season)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(season.additions, it.id)
                    }
            }

    override suspend fun updateAnime(anime: Anime): Anime =
        check(anime.id != ULong.MIN_VALUE) { "只能更新数据" }
            .let {
                animeRepository.updateAnime(anime)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(anime.additions, it.id)
                        this.refreshAnimeCache()
                    }
            }

    override suspend fun updateAnimeSeason(season: AnimeSeason): AnimeSeason =
        check(season.id != ULong.MIN_VALUE) { "只能更新数据" }
            .let {
                animeRepository.updateAnimeSeason(season)
                    .also {
                        additionalInfoRepository.saveAdditionalInfos(season.additions, it.id)
                    }
            }

    override suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason =
        season.copy(
            animeId =
                if (season.animeId == ULong.MIN_VALUE) {
                    // 新增动画系列
                    val anime = season.anime
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

    override suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile) {
        this.getAnimeSeasonsWithAdditionAndAnimeById(animeSeasonFile.id)
            .let {
                FileTool().handleAnimeSeasonFile(it, animeSeasonFile)
                // 更新文件状态
                val addition = AnimeAdditionType.FileStatus.additionalInfo(it.additions)
                    ?.copy(additionalValue = Status.PROCESSED.key)
                    ?: AdditionalInfo(
                        "",
                        it.id,
                        AnimeAssociatedType.ANIME_SEASON.key,
                        AnimeAdditionType.FileStatus.key,
                        Status.PROCESSED.key
                    )
                additionalInfoRepository.saveAdditionalInfo(addition)
            }
    }

    override suspend fun refreshAnimeCache() = AnimeCacheTool.refreshAnimeMap(this.getAllAnimeWithAdditionFromDB())

    override suspend fun removeAnimeById(id: ULong) {
        animeRepository.deleteAnimeById(id)
        animeRepository.deleteAnimeSeasonByAnimeId(id)
        animeRepository.deleteAnimeEpisodeByAnimeId(id)
        this.refreshAnimeCache()
    }

    override suspend fun removeAnimeSeasonById(id: ULong) {
        animeRepository.deleteAnimeSeasonById(id)
        animeRepository.deleteAnimeEpisodeBySeasonId(id)
    }

    override suspend fun removeAnimeEpisodeById(id: ULong) {
        animeRepository.deleteAnimeEpisodeById(id)
    }
}
