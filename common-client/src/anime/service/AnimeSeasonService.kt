package dev.sunriseydy.acgn.client.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccess
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
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
    fun loadData(onSuccess: (MutableMap<String, List<AnimeSeason>>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.getAnimeSeasonSectionMap().onSuccessData(appState, onSuccess, onError)
        }
    }

    fun searchAnimeSeasonSectionMapByName(name: String, onSuccess: (MutableMap<String, List<AnimeSeason>>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.searchAnimeSeasonSectionMapByName(name).onSuccessData(appState, onSuccess, onError)
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

    fun refreshTmdbData(season: AnimeSeason, onSuccess: () -> Unit = { }, onError: (String) -> Unit = { }) {
        val showId = season.anime?.tmdbId?.toInt()
        if (showId == null) {
            onError("该动漫季度关联的动画没有 TMDB ID")
            return
        }
        appState.scope.launch {
            appState.api.anime.getTmdbAnimeSeasonDetail(showId, season.season.toString()).onSuccessData(
                appState,
                onSuccess = { tmdbSeason ->
                    val tmdbAddition = AnimeAdditionType.TmdbJson.additionalInfo(tmdbSeason.additions)!!
                    val updatedSeason = tmdbSeason.copy(
                        id = season.id,
                        animeId = season.animeId,
                        bgmId = season.bgmId,
                        // 附加信息，如果存在 tmdb 的，则更新值，否则取接口返回的
                        additions = listOf(AnimeAdditionType.TmdbJson.additionalInfo(season.additions)?.copy(additionalValue = tmdbAddition.additionalValue)
                            ?: tmdbAddition
                        )
                    )
                    saveAnimeSeason(updatedSeason, onSuccess = { onSuccess() }, onError = onError)
                },
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