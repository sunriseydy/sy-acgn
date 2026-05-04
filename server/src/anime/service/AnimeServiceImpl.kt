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
 * 动漫服务实现类
 *
 * 负责处理动漫相关的业务逻辑，包括数据的增删改查、缓存管理以及与附加信息的组合。
 *
 * @property animeRepository 动漫数据仓库
 * @property additionalInfoRepository 附加信息仓库
 * @author SunriseYDY
 * @date 2024-07-04 18:41
 */
class AnimeServiceImpl(
    val animeRepository: AnimeRepository,
    val additionalInfoRepository: AdditionalInfoRepository
) : AnimeService {
    /**
     * 从数据库获取所有动漫，并附带附加信息
     */
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

    /**
     * 根据名称搜索动漫
     *
     * 优先使用缓存。如果缓存为空，则先刷新缓存。
     * 如果名称为空，返回所有动漫列表。
     */
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

    /**
     * 根据 ID 获取动漫及其关联的附加信息和所属动漫信息
     */
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

    /**
     * 获取按年份和月份分组的动漫季度列表
     *
     * 返回一个 Map，键为 "年份 - 季节"，值为对应的动漫季度列表。
     * 优化：每个年份只查询一次数据库，按月份类型在内存中分组，避免 N+1 查询。
     */
    override suspend fun getAnimeSeasonSectionMap(): MutableMap<String, List<AnimeSeason>> {
        val sectionMap: MutableMap<String, List<AnimeSeason>> = mutableMapOf()
        val years = getAnimeSeasonYears()
        for (year in years) {
            // 一次查询获取该年份的所有季度和附加信息
            val allSeasonsForYear = getAnimeSeasonsWithAdditionAndAnimeByYearAndMonth(year, null)
            // 在内存中按月份类型分组
            for (monthType in AnimeMonthType.entries) {
                val filtered = allSeasonsForYear.filter { it.month in monthType.months }
                if (filtered.isNotEmpty()) {
                    sectionMap["$year - ${monthType.meaning}"] = filtered
                }
            }
        }
        return sectionMap
    }

    override suspend fun searchAnimeSeasonSectionMapByName(name: String): MutableMap<String, List<AnimeSeason>> {
        val sectionMap: MutableMap<String, List<AnimeSeason>> = mutableMapOf()
        if (name.isBlank()) {
            return getAnimeSeasonSectionMap()
        }
        val animes = searchAnimeByName(name)
        if (animes.isEmpty()) {
            return sectionMap
        }

        val allSeasons = animes.flatMap { anime ->
            getAnimeSeasonsWithAdditionByAnimeId(anime.id).map { season ->
                season.copy(anime = anime)
            }
        }

        if (allSeasons.isEmpty()) {
            return sectionMap
        }

        val years = allSeasons.map { it.year }.distinct().sortedDescending()
        for (year in years) {
            val allSeasonsForYear = allSeasons.filter { it.year == year }
            for (monthType in AnimeMonthType.entries) {
                val filtered = allSeasonsForYear.filter { it.month in monthType.months }
                if (filtered.isNotEmpty()) {
                    sectionMap["$year - ${monthType.meaning}"] = filtered
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

    /**
     * 保存动漫季度信息
     *
     * 如果关联的动漫不存在（ID 为 MIN_VALUE），则先创建动漫。
     * 如果季度 ID 为 MIN_VALUE，则创建季度，否则更新季度。
     * 最终返回包含完整信息的动漫季度对象。
     */
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

    /**
     * 处理动漫季度相关的本地文件
     *
     * 调用 FileTool 处理文件，并更新文件处理状态为 "已处理"。
     */
    override suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile) {
        this.getAnimeSeasonsWithAdditionAndAnimeById(animeSeasonFile.id)
            .let {
                FileTool.handleAnimeSeasonFile(it, animeSeasonFile)
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
