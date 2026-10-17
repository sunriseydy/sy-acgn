package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService

/**
 * 动画与季度搜索选择的共享状态
 *
 * 抽取创建动画季度相关弹窗（[CreateAnimeSeason]、[CreateAnimeSeasonFromFile]）
 * 中公共的搜索与选择逻辑，供两个弹窗的状态类组合复用。
 *
 * @author SunriseYDY
 * @date 2026-10-17 21:54
 */
class AnimeSeasonSearchState(
    private val service: AnimeSeasonService
) {
    /** 动画名称搜索词 */
    val animeNameSearch: MutableState<String> = mutableStateOf("")

    /** 动画搜索结果及下拉可见状态 */
    val animeSearchResult: MutableState<List<Anime>> = mutableStateOf(emptyList())
    val animeSearchVisible: MutableState<Boolean> = mutableStateOf(false)

    /** 选中的动画 */
    val selectedAnime: MutableState<Anime?> = mutableStateOf(null)

    /** 动画季度搜索结果及下拉可见状态 */
    val animeSeasonSearchResult: MutableState<List<AnimeSeason>> = mutableStateOf(emptyList())
    val animeSeasonSearchVisible: MutableState<Boolean> = mutableStateOf(false)

    /** 选中的动画季度 */
    val selectedAnimeSeason: MutableState<AnimeSeason?> = mutableStateOf(null)

    /** 清空搜索词与全部搜索/选择状态 */
    fun reset() {
        animeNameSearch.value = ""
        resetSelection()
    }

    /** 仅清空搜索结果与选择状态，保留搜索词 */
    fun resetSelection() {
        animeSearchVisible.value = false
        animeSearchResult.value = emptyList()
        selectedAnime.value = null
        animeSeasonSearchVisible.value = false
        animeSeasonSearchResult.value = emptyList()
        selectedAnimeSeason.value = null
    }

    /**
     * 按名称搜索动画
     *
     * @param isCreateAnime 为 true 时搜索 TMDB，否则搜索本地
     */
    fun searchAnime(isCreateAnime: Boolean, onError: (String) -> Unit = { }) {
        if (animeNameSearch.value.isNotBlank()) {
            if (isCreateAnime) {
                service.searchAnimeFromTMDB(
                    name = animeNameSearch.value,
                    onSuccess = {
                        animeSearchResult.value = it
                        animeSearchVisible.value = true
                    },
                    onError = onError
                )
            } else {
                service.searchAnime(
                    name = animeNameSearch.value,
                    onSuccess = {
                        animeSearchResult.value = it
                        animeSearchVisible.value = true
                    },
                    onError = onError
                )
            }
        }
    }

    fun selectAnime(selected: Anime) {
        selectedAnime.value = selected
        animeNameSearch.value = selected.name
        animeSearchVisible.value = false
        animeSearchResult.value = emptyList()
        selectedAnimeSeason.value = null
    }

    /**
     * 搜索选中动画的季度列表
     *
     * @param isCreateAnime 为 true 时直接使用 TMDB 动画搜索结果自带的季度，否则按 TMDB ID 拉取
     */
    fun searchSeason(isCreateAnime: Boolean, onError: (String) -> Unit = { }) {
        selectedAnime.value?.let { anime ->
            if (isCreateAnime && anime.animeSeasons.isNotEmpty()) {
                animeSeasonSearchResult.value = anime.animeSeasons
                animeSeasonSearchVisible.value = true
            } else {
                anime.tmdbId?.let { tmdbId ->
                    service.getAnimeByTmdbId(
                        id = tmdbId,
                        onSuccess = {
                            animeSeasonSearchResult.value = it.animeSeasons
                            animeSeasonSearchVisible.value = true
                        },
                        onError = onError
                    )
                }
            }
        }
    }

    fun selectSeason(season: AnimeSeason) {
        val anime = selectedAnime.value ?: return
        val showId = anime.tmdbId?.toInt()
        // 选中季度后，尽量拉取 TMDB 季度详情（含集数）
        if (showId != null) {
            service.getTmdbAnimeSeasonDetail(
                showId = showId,
                season = season.season.toString(),
                onSuccess = { detail ->
                    selectedAnimeSeason.value = detail.copy(
                        animeId = anime.id,
                        anime = anime,
                        bgmId = season.bgmId,
                    )
                },
                onError = {
                    selectedAnimeSeason.value = season.copy(animeId = anime.id, anime = anime)
                }
            )
        } else {
            selectedAnimeSeason.value = season.copy(animeId = anime.id, anime = anime)
        }
        animeSeasonSearchVisible.value = false
        animeSeasonSearchResult.value = emptyList()
    }
}
