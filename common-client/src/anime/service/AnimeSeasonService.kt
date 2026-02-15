package dev.sunriseydy.acgn.client.anime.service

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccess
import dev.sunriseydy.acgn.client.base.api.onSuccessData

/**
 * @author SunriseYDY
 * @date 2025-02-15 11:39
 */
class AnimeSeasonService(
    val appState: AppState,
) {
    fun loadData(onSuccess: (MutableMap<String, List<AnimeSeason>>) -> Unit, onError: (String) -> Unit = { }) {
        appState.api.anime.getAnimeSeasonSectionMap().onSuccessData(appState, onSuccess, onError)
    }

    fun saveAnimeSeason(
        animeSeason: AnimeSeason,
        onSuccess: (AnimeSeason) -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        appState.api.anime.saveAnimeSeason(animeSeason).onSuccessData(appState, onSuccess, onError)
    }

    fun searchAnime(name: String, onSuccess: (List<Anime>) -> Unit, onError: (String) -> Unit = { }) {
        appState.api.anime.searchAnimeByName(name).onSuccessData(appState, onSuccess, onError)
    }

    fun searchAnimeFromTMDB(name: String, onSuccess: (List<Anime>) -> Unit, onError: (String) -> Unit = { }) {
        appState.api.anime.searchTmdbAnimeTv(name).onSuccessData(appState, onSuccess, onError)
    }

    fun getAnimeByTmdbId(id: ULong, onSuccess: (Anime) -> Unit, onError: (String) -> Unit = { }) {
        appState.api.anime.getTmdbAnimeTvDetail(id).onSuccessData(appState, onSuccess, onError)
    }

    fun deleteSeason(id: ULong, onSuccess: () -> Unit = { }) {
        appState.api.anime.removeAnimeSeasonById(id).onSuccess(appState, onSuccess)
    }

    fun handleAnimeSeasonFile(
        seasonFile: AnimeSeasonFile,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { }
    ) {
        appState.api.anime.handleAnimeSeasonFile(seasonFile)
            .onSuccess(appState, onSuccess = onSuccess, onError = onError)
    }

    fun searchBgmAnime(query: String, onSuccess: (List<AnimeSeason>) -> Unit, onError: (String) -> Unit = { }) {
        appState.api.anime.searchBgmAnime(query).onSuccessData(appState, onSuccess, onError)
    }
}