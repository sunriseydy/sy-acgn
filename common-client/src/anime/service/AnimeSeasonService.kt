package dev.sunriseydy.acgn.client.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.dto.BangumiSeasonalAnimeResult
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccess
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.common.dto.OpenAiChatRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 动漫季度服务层
 *
 * 封装动漫季度相关的业务逻辑，协调 API 调用和 UI 状态更新。
 * 包括数据加载、TMDB/Bangumi 搜索、季度保存/删除和文件处理。
 *
 * @param appState 应用全局状态
 *
 * @author SunriseYDY
 * @date 2025-02-15 11:39
 */
class AnimeSeasonService(
    val appState: AppState,
) {
    fun loadData(
        name: String? = null,
        fromDb: Boolean = false,
        onSuccess: (MutableMap<String, List<AnimeSeason>>) -> Unit,
        onError: (String) -> Unit = { }
    ) {
        appState.scope.launch {
            appState.api.anime.getAnimeSeasonSectionMap(name, fromDb).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun saveAnimeSeason(
        animeSeason: AnimeSeason,
        onSuccess: (AnimeSeason) -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        appState.scope.launch {
            appState.api.anime.saveAnimeSeason(animeSeason).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun searchAnime(name: String, onSuccess: (List<Anime>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.searchAnimeByName(name).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun searchAnimeSeason(name: String, onSuccess: (List<AnimeSeason>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.searchAnimeSeasonByName(name).onSuccessData(appState, onSuccess, onError)
        }
    }

    /**
     * 通过 AI 从下载目录名中识别动画名
     *
     * 流式接收 AI 输出并通过 [onDelta] 逐段回调，完成后通过 [onComplete] 返回完整名称。
     * 返回 [Job] 以便调用方在弹窗关闭等场景取消尚未完成的识别请求。
     *
     * @param blankNameMessage 未识别出名称时的错误消息（由调用方本地化）
     * @param failedMessage 识别异常时的兜底错误消息（由调用方本地化）
     */
    fun extractAnimeNameByAi(
        directoryPath: String,
        blankNameMessage: String,
        failedMessage: String,
        onDelta: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit = { }
    ): Job {
        val trimmed = directoryPath.trim().trimEnd('/', '\\')
        val dirName = trimmed.substringAfterLast('/').substringAfterLast('\\').ifBlank { trimmed }
        val request = OpenAiChatRequest.simple(
            prompt = dirName,
            systemPrompt = "你是一个动画媒体管理助手。用户的输入是下载好的动画目录名或种子发布名称。请从中识别并提取出最核心的动画主标题（优先提取常用中文名，无中文名则保留原名或官方英文名）。要求：仅直接输出动画名称本身，不要包含字幕组、分辨率、编码、第几季或集数等额外信息，不要有任何多余解释、标点或换行。",
            temperature = 0.1,
        )
        return appState.scope.launch {
            try {
                val sb = StringBuilder()
                appState.api.common.chatStreamFlow(request).collect { chunk ->
                    val delta = chunk.textDelta
                    if (delta.isNotEmpty()) {
                        sb.append(delta)
                        onDelta(sb.toString().trim())
                    }
                }
                val resultName = sb.toString().trim().trim('"', '\'', '`', '《', '》')
                if (resultName.isBlank()) {
                    onError(blankNameMessage)
                } else {
                    onComplete(resultName)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError(e.message ?: failedMessage)
            }
        }
    }

    fun searchAnimeFromTMDB(name: String, onSuccess: (List<Anime>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.searchTmdbAnimeTv(name).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun getAnimeByTmdbId(id: ULong, onSuccess: (Anime) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.getTmdbAnimeTvDetail(id).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun getTmdbAnimeSeasonDetail(
        showId: Int,
        season: String,
        onSuccess: (AnimeSeason) -> Unit,
        onError: (String) -> Unit = { }
    ) {
        appState.scope.launch {
            appState.api.anime.getTmdbAnimeSeasonDetail(showId, season).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun loadSeasonById(
        seasonId: ULong,
        onSuccess: (AnimeSeason) -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        appState.scope.launch {
            appState.api.anime.getAnimeSeasonById(seasonId).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun loadEpisodes(
        seasonId: ULong,
        onSuccess: (List<AnimeEpisode>) -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        appState.scope.launch {
            appState.api.anime.getAnimeEpisodesBySeasonId(seasonId).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun syncEpisodes(
        seasonId: ULong,
        onSuccess: (List<AnimeEpisode>) -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        appState.scope.launch {
            appState.api.anime.syncAnimeSeasonEpisodes(seasonId).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun refreshTmdbData(season: AnimeSeason, onSuccess: () -> Unit = { }, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            // 同步元数据与集数
            appState.api.anime.syncAnimeSeasonEpisodes(season.id).onSuccessData(
                appState,
                onSuccess = { onSuccess() },
                onError = onError
            )
        }
    }

    fun deleteSeason(id: ULong, onSuccess: () -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.removeAnimeSeasonById(id).onSuccess(appState, onSuccess)
        }
    }

    fun handleAnimeSeasonFile(
        seasonFile: AnimeSeasonFile,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        appState.scope.launch {
            appState.api.anime.handleAnimeSeasonFile(seasonFile)
                .onSuccess(appState, onSuccess = onSuccess, onError = onError)
        }
    }

    fun searchBgmAnime(query: String, onSuccess: (List<AnimeSeason>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.searchBgmAnime(query).onSuccessData(appState, onSuccess, onError)
        }
    }

    fun getSeasonalAnime(
        year: Int,
        monthType: AnimeMonthType,
        query: String? = null,
        sort: String = "heat",
        limit: Int = 20,
        offset: Int = 0,
        onSuccess: (BangumiSeasonalAnimeResult) -> Unit,
        onError: (String) -> Unit = { }
    ) {
        appState.scope.launch {
            appState.api.anime.getBangumiSeasonalAnime(
                year = year,
                monthType = monthType,
                query = query,
                sort = sort,
                limit = limit,
                offset = offset
            ).onSuccessData(appState, onSuccess, onError)
        }
    }
}