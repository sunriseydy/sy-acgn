package dev.sunriseydy.acgn.server.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
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
import dev.sunriseydy.acgn.server.anime.tools.TmdbTool
import dev.sunriseydy.acgn.server.anime.tools.tmdb.image.TmdbImageSize
import dev.sunriseydy.acgn.server.anime.tools.tmdb.image.TmdbImageUrlBuilder
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository
import dev.sunriseydy.acgn.server.common.service.AttachFileInfoService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

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
    val additionalInfoRepository: AdditionalInfoRepository,
    val attachFileInfoService: AttachFileInfoService,
    val tmdbTool: TmdbTool = TmdbTool(),
) : AnimeService {
    private val logger = KotlinLogging.logger { }

    suspend fun getAllAnime(): List<Anime> {
        val additionalInfos = additionalInfoRepository.selectAdditionalInfos(AnimeAssociatedType.ANIME.key)
        return animeRepository.selectAllAnime().map {
            it.copy(
                additions = additionalInfos.filter { info -> info.associatedId == it.id }
            )
        }
    }

    suspend fun getAllAnimeSeasons(): List<AnimeSeason> {
        val additionalInfos = additionalInfoRepository.selectAdditionalInfos(AnimeAssociatedType.ANIME_SEASON.key)
        return animeRepository.selectAllAnimeSeasons().map {
            it.copy(
                anime = this.getAnimeById(it.animeId),
                additions = additionalInfos.filter { info -> info.associatedId == it.id }
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
        if (AnimeCacheTool.isAnimeEmpty()) {
            this.refreshCache()
        }
        return if (name.isNullOrBlank()) {
            AnimeCacheTool.getAnimeList()
        } else {
            AnimeCacheTool.getAnimeList().filter { it.name.contains(name, ignoreCase = true) }
        }
    }

    suspend fun getAnimeById(id: ULong): Anime {
        return AnimeCacheTool.getAnimeById(id) ?: this.addAnimeCache(id)
    }

    override suspend fun getAnimeSeasonById(id: ULong): AnimeSeason {
        val season = AnimeCacheTool.getSeason(id) ?: this.addAnimeSeasonCache(id)
        // 详情接口附带集数列表
        return season.copy(animeEpisodes = this.getAnimeEpisodesBySeasonId(id))
    }

    override suspend fun getAnimeEpisodesBySeasonId(seasonId: ULong): List<AnimeEpisode> {
        return animeRepository.selectAnimeEpisodeBySeasonId(seasonId).map { episode ->
            episode.copy(
                additions = additionalInfoRepository.selectAdditionalInfos(
                    AnimeAssociatedType.ANIME_EPISODE.key,
                    episode.id
                )
            )
        }
    }

    suspend fun getAnimeSeasonsByAnimeId(animeId: ULong): List<AnimeSeason> {
        val seasonIds = if (AnimeCacheTool.isAnimeSeasonEmpty(animeId)) {
            animeRepository.selectAnimeSeasonByAnimeId(animeId).map { it.id }
        } else {
            AnimeCacheTool.getAnimeSeasons(animeId)!!
        }
        return seasonIds.map {
            // 列表场景不需要整包集数，直接取缓存本体
            AnimeCacheTool.getSeason(it) ?: this.addAnimeSeasonCache(it)
        }
    }

    /**
     * 获取按年份和月份分组的动漫季度列表
     *
     * 返回一个 Map，键为 "年份 - 季节"，值为对应的动漫季度列表。
     */
    override suspend fun getAnimeSeasonSectionMap(name: String?): MutableMap<String, List<AnimeSeason>> {
        val sectionMap: MutableMap<String, List<AnimeSeason>> = mutableMapOf()
        var seasons: List<AnimeSeason>
        if (AnimeCacheTool.isSeasonEmpty()) {
            this.refreshCache()
        }
        if (name.isNullOrBlank()) {
            seasons = AnimeCacheTool.getSeasons()
        } else {
            val anime = searchAnimeByName(name)
            if (anime.isEmpty()) {
                return sectionMap
            }
            seasons = anime.flatMap { animeItem ->
                getAnimeSeasonsByAnimeId(animeItem.id)
            }
        }
        if (seasons.isEmpty()) {
            return sectionMap
        }
        val years = seasons.map { it.year }.distinct().sortedDescending()
        for (year in years) {
            val allSeasonsForYear = seasons.filter { it.year == year }
            for (monthType in AnimeMonthType.entries.reversed()) {
                val filtered = allSeasonsForYear.filter { it.month in monthType.months }
                if (filtered.isNotEmpty()) {
                    sectionMap["$year - ${monthType.meaning}"] = filtered
                }
            }
        }
        return sectionMap
    }

    suspend fun createAnime(anime: Anime): Anime {
        check(anime.id == ULong.MIN_VALUE) { "只能新增数据" }
        return animeRepository.insertAnime(anime).let {
            additionalInfoRepository.saveAdditionalInfos(anime.additions, it.id)
            return@let this.addAnimeCache(it.id)
        }
    }

    private suspend fun downloadAndSavePoster(seasonId: ULong, additions: List<AdditionalInfo>): AdditionalInfo? {
        val tmdbJson = AnimeAdditionType.TmdbJson.valueOf(additions) ?: return null
        val posterPath = tmdbJson["poster_path"]?.jsonPrimitive?.contentOrNull ?: return null
        if (posterPath.isBlank()) return null

        val existingPosterId = AnimeAdditionType.PosterId.valueOf(additions)
        if (!existingPosterId.isNullOrBlank()) {
            return null
        }

        try {
            val imageUrl = TmdbImageUrlBuilder.build(posterPath, TmdbImageSize.ORIGINAL)
            val attachFileId = attachFileInfoService.saveFile(
                downloadUrl = imageUrl,
                defaultContentType = "image/jpeg",
                defaultFileName = posterPath.substringAfterLast("/").ifBlank { "poster.jpg" }
            )
            return AdditionalInfo(
                id = "",
                associatedId = seasonId,
                associatedType = AnimeAssociatedType.ANIME_SEASON.key,
                additionalType = AnimeAdditionType.PosterId.key,
                additionalValue = attachFileId
            )
        } catch (e: Exception) {
            logger.error(e) { "Error downloading poster for season $seasonId" }
        }
        return null
    }

    override suspend fun createAnimeSeason(season: AnimeSeason): AnimeSeason {
        // 只能新增数据
        check(season.id == ULong.MIN_VALUE) { "只能新增数据" }
        return animeRepository.insertAnimeSeason(season)
            .let { insertedSeason ->
                val additions = season.additions.toMutableList()
                val posterAddition = downloadAndSavePoster(insertedSeason.id, additions)
                if (posterAddition != null) {
                    additions.add(posterAddition)
                }
                additionalInfoRepository.saveAdditionalInfos(additions, insertedSeason.id)
                // 创建季度时同步写入集数（若调用方已携带 TMDB 集数）
                if (season.animeEpisodes.isNotEmpty()) {
                    upsertAnimeEpisodes(
                        animeId = insertedSeason.animeId,
                        seasonId = insertedSeason.id,
                        episodes = season.animeEpisodes
                    )
                }
                return@let this.addAnimeSeasonCache(insertedSeason.id)
            }
    }

    suspend fun updateAnime(anime: Anime): Anime {
        check(anime.id != ULong.MIN_VALUE) { "只能更新数据" }
        return animeRepository.updateAnime(anime)
            .let {
                additionalInfoRepository.saveAdditionalInfos(anime.additions, it.id)
                return@let this.addAnimeCache(it.id)
            }
    }

    suspend fun updateAnimeSeason(season: AnimeSeason): AnimeSeason {
        check(season.id != ULong.MIN_VALUE) { "只能更新数据" }
        val additions = season.additions.toMutableList()
        val posterAddition = downloadAndSavePoster(season.id, additions)
        if (posterAddition != null) {
            additions.add(posterAddition)
        }
        return animeRepository.updateAnimeSeason(season)
            .let {
                additionalInfoRepository.saveAdditionalInfos(additions, it.id)
                return@let this.addAnimeSeasonCache(it.id)
            }
    }

    /**
     * 保存动漫季度信息
     *
     * 如果关联的动漫不存在（ID 为 MIN_VALUE），则先创建动漫。
     * 如果季度 ID 为 MIN_VALUE，则创建季度，否则更新季度。
     * 最终返回包含完整信息的动漫季度对象。
     */
    override suspend fun saveAnimeSeason(season: AnimeSeason): AnimeSeason {
        val animeId =
            if (season.animeId == ULong.MIN_VALUE) {
                // 新增动画系列
                val anime = season.anime
                checkNotNull(anime) { "新增动画时的动画数据为空" }
                this.createAnime(anime).id
            } else {
                // 动画系列已存在
                season.animeId
            }
        return season.copy(
            animeId = animeId
        ).let {
            val saved = if (it.id == ULong.MIN_VALUE) {
                this.createAnimeSeason(it)
            } else {
                this.updateAnimeSeason(it)
            }
            // 更新季度时若携带集数列表，则同步 upsert
            if (it.id != ULong.MIN_VALUE && it.animeEpisodes.isNotEmpty()) {
                upsertAnimeEpisodes(
                    animeId = saved.animeId,
                    seasonId = saved.id,
                    episodes = it.animeEpisodes
                )
                return@let this.addAnimeSeasonCache(saved.id)
            }
            saved
        }
    }

    /**
     * 从 TMDB 同步指定季度的集数列表
     */
    override suspend fun syncAnimeSeasonEpisodes(seasonId: ULong): List<AnimeEpisode> {
        val season = AnimeCacheTool.getSeason(seasonId) ?: this.addAnimeSeasonCache(seasonId)
        val showId = season.anime?.tmdbId?.toInt()
            ?: throw IllegalArgumentException("该动漫季度关联的动画没有 TMDB ID")
        val tmdbSeason = tmdbTool.getTvSeasonDetailsForAnimeSeason(showId, season.season)
        // 同步季度元数据（集数总数等）
        val tmdbAddition = AnimeAdditionType.TmdbJson.additionalInfo(tmdbSeason.additions)
        val mergedAdditions = if (tmdbAddition != null) {
            val existing = AnimeAdditionType.TmdbJson.additionalInfo(season.additions)
            listOf(
                existing?.copy(additionalValue = tmdbAddition.additionalValue)
                    ?: tmdbAddition.copy(associatedId = season.id)
            ) + season.additions.filter { it.additionalType != AnimeAdditionType.TmdbJson.key }
        } else {
            season.additions
        }
        this.updateAnimeSeason(
            season.copy(
                name = tmdbSeason.name.ifBlank { season.name },
                description = tmdbSeason.description ?: season.description,
                numberOfEpisodes = tmdbSeason.numberOfEpisodes,
                airDate = tmdbSeason.airDate ?: season.airDate,
                year = tmdbSeason.year.takeIf { it > 0 } ?: season.year,
                month = tmdbSeason.month.takeIf { it > 0 } ?: season.month,
                tmdbId = tmdbSeason.tmdbId ?: season.tmdbId,
                additions = mergedAdditions,
            )
        )
        return upsertAnimeEpisodes(
            animeId = season.animeId,
            seasonId = season.id,
            episodes = tmdbSeason.animeEpisodes
        ).also {
            this.addAnimeSeasonCache(season.id)
        }
    }

    private suspend fun upsertAnimeEpisodes(
        animeId: ULong,
        seasonId: ULong,
        episodes: List<AnimeEpisode>,
    ): List<AnimeEpisode> {
        val existing = animeRepository.selectAnimeEpisodeBySeasonId(seasonId)
            .associateBy { it.episode }
        return episodes
            .sortedBy { it.episode }
            .map { source ->
                val prepared = source.copy(
                    animeId = animeId,
                    animeSeasonId = seasonId,
                )
                val matched = existing[prepared.episode]
                val saved = if (matched == null) {
                    animeRepository.insertAnimeEpisode(prepared.copy(id = ULong.MIN_VALUE))
                } else {
                    animeRepository.updateAnimeEpisode(
                        prepared.copy(
                            id = matched.id,
                            bgmId = prepared.bgmId ?: matched.bgmId,
                        )
                    )
                }
                if (prepared.additions.isNotEmpty()) {
                    additionalInfoRepository.saveAdditionalInfos(prepared.additions, saved.id)
                }
                saved.copy(
                    additions = additionalInfoRepository.selectAdditionalInfos(
                        AnimeAssociatedType.ANIME_EPISODE.key,
                        saved.id
                    )
                )
            }
    }

    /**
     * 处理动漫季度相关的本地文件
     *
     * 调用 FileTool 处理文件，并更新文件处理状态为 "已处理"。
     */
    override suspend fun handleAnimeSeasonFile(animeSeasonFile: AnimeSeasonFile) {
        val season = AnimeCacheTool.getSeason(animeSeasonFile.id) ?: this.addAnimeSeasonCache(animeSeasonFile.id)
        FileTool.handleAnimeSeasonFile(season, animeSeasonFile)
        // 更新文件状态
        val addition = AnimeAdditionType.FileStatus.additionalInfo(season.additions)
            ?.copy(additionalValue = Status.PROCESSED.key)
            ?: AdditionalInfo(
                "",
                season.id,
                AnimeAssociatedType.ANIME_SEASON.key,
                AnimeAdditionType.FileStatus.key,
                Status.PROCESSED.key
            )
        additionalInfoRepository.saveAdditionalInfo(addition)
        this.addAnimeSeasonCache(season.id)
    }

    override suspend fun removeAnimeSeasonById(id: ULong) {
        animeRepository.deleteAnimeSeasonById(id)
        animeRepository.deleteAnimeEpisodeBySeasonId(id)
        AnimeCacheTool.removeSeason(id)
    }

    override suspend fun removeAnimeEpisodeById(id: ULong) {
        animeRepository.deleteAnimeEpisodeById(id)
    }

    suspend fun refreshAnimeCache() {
        AnimeCacheTool.refreshAnimeCache(this.getAllAnime())
    }

    suspend fun refreshSeasonCache() {
        AnimeCacheTool.refreshSeasonCache(this.getAllAnimeSeasons())
    }

    override suspend fun refreshCache() {
        refreshAnimeCache()
        refreshSeasonCache()
    }

    suspend fun addAnimeCache(id: ULong): Anime {
        return AnimeCacheTool.setAnime(this.animeRepository.selectAnimeById(id).let {
            it.copy(additions = additionalInfoRepository.selectAdditionalInfos(AnimeAssociatedType.ANIME.key, it.id))
        })
    }

    suspend fun addAnimeSeasonCache(id: ULong): AnimeSeason {
        return AnimeCacheTool.setSeason(this.animeRepository.selectAnimeSeasonById(id).let {
            it.copy(
                anime = this.getAnimeById(it.animeId),
                additions = additionalInfoRepository.selectAdditionalInfos(AnimeAssociatedType.ANIME_SEASON.key, it.id)
            )
        })
    }
}
