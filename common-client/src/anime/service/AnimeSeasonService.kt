package dev.sunriseydy.acgn.client.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccess
import dev.sunriseydy.acgn.client.base.api.onSuccessData
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
    fun loadData(name: String? = null, onSuccess: (MutableMap<String, List<AnimeSeason>>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.getAnimeSeasonSectionMap(name).onSuccessData(appState, onSuccess, onError)
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
}