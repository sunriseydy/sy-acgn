package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.client.anime.enums.AnimeString
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.utils.RequiredSupportingText

/**
 * 创建动画季度弹窗
 *
 * @author SunriseYDY
 * @date 2025-02-15 11:32
 */
@Composable
fun CreateAnimeSeason(
    animeSeasonService: AnimeSeasonService,
    createDialogVisible: MutableState<Boolean>,
    onSuccess: (AnimeSeason) -> Unit = { }
) {
    val isCreateAnime: MutableState<Boolean> = remember { mutableStateOf(false) }

    val animeSearchVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val animeNameSearch: MutableState<String> = remember { mutableStateOf("") }
    val animeSearchResult: MutableState<List<Anime>> = remember { mutableStateOf(emptyList()) }

    val anime: MutableState<Anime?> = remember { mutableStateOf(null) }

    val animeSeason: MutableState<AnimeSeason?> = remember { mutableStateOf(null) }

    val animeSeasonSearchVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val animeSeasonSearchResult: MutableState<List<AnimeSeason>> = remember { mutableStateOf(emptyList()) }

    fun resetField() {
        animeSearchVisible.value = false
        animeNameSearch.value = ""
        animeSearchResult.value = emptyList()

        anime.value = null

        animeSeason.value = null

        animeSeasonSearchVisible.value = false
        animeSeasonSearchResult.value = emptyList()
    }

    fun closeCreateDialog() {
        createDialogVisible.value = false
        isCreateAnime.value = false

        resetField()
    }

    fun handleSave() {
        requireNotNull(anime.value) { AnimeString.SEASON_FIELD_ANIME_NAME.localization + CommonString.IS_BLANK.localization }
        requireNotNull(
            animeSeason.value,
            lazyMessage = { AnimeString.SEASON_FIELD_SEASON_NAME.localization + CommonString.IS_BLANK.localization })
            .let {
                animeSeasonService.saveAnimeSeason(it, onSuccess)
                closeCreateDialog()
            }
    }

    fun handleAnimeNameSearch() {
        if (animeNameSearch.value.isNotBlank()) {
            if (isCreateAnime.value) {
                animeSeasonService.searchAnimeFromTMDB(name = animeNameSearch.value, onSuccess = {
                    animeSearchResult.value = it
                    animeSearchVisible.value = true
                })
            } else {
                animeSeasonService.searchAnime(name = animeNameSearch.value, onSuccess = {
                    animeSearchResult.value = it
                    animeSearchVisible.value = true
                })
            }
        }
    }

    fun handleAnimeSeasonNameSearch() {
        anime.value?.let { anime ->
            if (isCreateAnime.value && anime.animeSeasons.isNotEmpty()) {
                // 如果创建动画，则直接取tmdb动画搜索结果的季度
                animeSeasonSearchResult.value = anime.animeSeasons
                animeSeasonSearchVisible.value = true
            } else {
                anime.tmdbId?.let { tmdbId ->
                    // 否则如果动画有tmdbId，则取tmdb动画季度
                    animeSeasonService.getAnimeByTmdbId(tmdbId, onSuccess = { anime ->
                        animeSeasonSearchResult.value = anime.animeSeasons
                        animeSeasonSearchVisible.value = true
                    })
                }
            }
        }
    }

    @Composable
    fun render() {
        FormDialog(
            formDialogVisible = createDialogVisible,
            onDismissRequest = { closeCreateDialog() },
            onConfirmation = { handleSave() },
        ) {
            val fieldWidth = 300.dp
            Row {
                Text(
                    AnimeString.SEASON_FIELD_IS_CREATE_ANIME.localization,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Checkbox(
                    checked = isCreateAnime.value,
                    onCheckedChange = {
                        isCreateAnime.value = it
                        resetField()
                    }
                )
            }

            // 动画名称搜索框
            OutlinedTextField(
                value = animeNameSearch.value,
                onValueChange = { animeNameSearch.value = it },
                modifier = Modifier.width(fieldWidth),
                label = {
                    RequiredFieldLabel(
                        AnimeString.SEASON_FIELD_ANIME_NAME_SEARCH.localization +
                                if (isCreateAnime.value) AnimeString.SEARCH_TMDB.localization else AnimeString.SEARCH_LOCAL.localization
                    )
                },
                supportingText = {
                    RequiredSupportingText(
                        animeNameSearch,
                        AnimeString.SEASON_FIELD_ANIME_NAME_SEARCH.localization
                    )
                },
                leadingIcon = {
                    IconButton(onClick = {
                        handleAnimeNameSearch()
                    }) {
                        Icon(Icons.Default.Search, null)
                    }
                },
            )
            // 动画搜索结果
            Box {
                DropdownMenu(
                    expanded = animeSearchVisible.value,
                    onDismissRequest = { animeSearchVisible.value = false },
                    scrollState = rememberScrollState(),
                    modifier = Modifier.width(fieldWidth).heightIn(max = fieldWidth)
                ) {
                    animeSearchResult.value.map {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                anime.value = it
                                animeNameSearch.value = it.name
                                animeSearchVisible.value = false
                                animeSearchResult.value = emptyList()
                            },
                        )
                    }
                    if (animeSearchResult.value.isEmpty()) {
                        Text(CommonString.NO_DATA.localization, modifier = Modifier.padding(8.dp))
                    }
                }
            }
            // 动画季度搜索框
            OutlinedTextField(
                value = if (animeSeason.value == null) "" else "${animeSeason.value!!.season} - ${animeSeason.value!!.name}",
                onValueChange = { },
                label = { RequiredFieldLabel(AnimeString.SEASON_FIELD_SEASON_NAME.localization) },
                modifier = Modifier.width(fieldWidth),
                readOnly = true,
                leadingIcon = {
                    IconButton(onClick = {
                        handleAnimeSeasonNameSearch()
                    }) {
                        Icon(Icons.Default.Search, null)
                    }
                },
            )
            // 动画季度搜索结果
            Box {
                DropdownMenu(
                    expanded = animeSeasonSearchVisible.value,
                    onDismissRequest = { animeSeasonSearchVisible.value = false },
                    scrollState = rememberScrollState(),
                    modifier = Modifier.width(fieldWidth).heightIn(max = fieldWidth)
                ) {
                    animeSeasonSearchResult.value.map {
                        DropdownMenuItem(
                            text = { Text(it.season.toString() + it.name) },
                            onClick = {
                                animeSeason.value = it.copy(animeId = anime.value!!.id, anime = anime.value)
                                animeSeasonSearchVisible.value = false
                                animeSeasonSearchResult.value = emptyList()
                            },
                        )
                    }
                    if (animeSeasonSearchResult.value.isEmpty()) {
                        Text(CommonString.NO_DATA.localization, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }

    render()
}