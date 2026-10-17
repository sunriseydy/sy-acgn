package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.base.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.base.utils.RequiredSupportingText
import dev.sunriseydy.acgn.client.res.*
import org.jetbrains.compose.resources.stringResource

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
    onSuccess: (AnimeSeason) -> Unit = { },
) {
    val state = rememberCreateAnimeSeasonState(animeSeasonService)
    val animeNameIsBlank =
        stringResource(Res.string.season_field_anime_name) + stringResource(Res.string.is_blank)
    val animeSeasonIsBlank =
        stringResource(Res.string.season_field_season_name) + stringResource(Res.string.is_blank)

    FormDialog(
        formDialogVisible = createDialogVisible,
        onDismissRequest = {
            createDialogVisible.value = false
            state.reset()
        },
        onConfirmation = {
            state.save(
                onSuccess = {
                    onSuccess(it)
                    createDialogVisible.value = false
                    state.reset()
                },
                animeNameIsBlankMsg = animeNameIsBlank,
                animeSeasonIsBlankMsg = animeSeasonIsBlank
            )
        },
    ) {
        val fieldWidth = 500.dp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    state.isCreateAnime.value = !state.isCreateAnime.value
                    state.resetField()
                }
                .padding(end = 8.dp)
        ) {
            Text(
                stringResource(Res.string.season_field_is_create_anime),
            )
            Checkbox(
                checked = state.isCreateAnime.value,
                onCheckedChange = {
                    state.isCreateAnime.value = it
                    state.resetField()
                }
            )
        }

        // 动画名称搜索框
        OutlinedTextField(
            value = state.animeNameSearch.value,
            onValueChange = { state.animeNameSearch.value = it },
            modifier = Modifier.width(fieldWidth),
            label = {
                RequiredFieldLabel(
                    stringResource(Res.string.season_field_anime_name_search) +
                            if (state.isCreateAnime.value) stringResource(Res.string.search_tmdb) else stringResource(
                                Res.string.search_local
                            )
                )
            },
            supportingText = {
                RequiredSupportingText(
                    state.animeNameSearch,
                    stringResource(Res.string.season_field_anime_name_search)
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    state.searchAnime()
                }) {
                    Icon(Icons.Default.Search, null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { state.searchAnime() })
        )
        // 动画搜索结果
        Box {
            DropdownMenu(
                expanded = state.animeSearchVisible.value,
                onDismissRequest = { state.animeSearchVisible.value = false },
                scrollState = rememberScrollState(),
                modifier = Modifier.width(fieldWidth).heightIn(max = fieldWidth)
            ) {
                state.animeSearchResult.value.forEach {
                    DropdownMenuItem(
                        text = { Text("${it.name}  ${it.firstAirDate}") },
                        onClick = {
                            state.selectAnime(it)
                        },
                    )
                }
                if (state.animeSearchResult.value.isEmpty()) {
                    Text(stringResource(Res.string.no_data), modifier = Modifier.padding(8.dp))
                }
            }
        }
        // 动画季度搜索框
        OutlinedTextField(
            value = if (state.animeSeason.value == null) "" else "${state.animeSeason.value!!.season} - ${state.animeSeason.value!!.name}",
            onValueChange = { },
            label = { RequiredFieldLabel(stringResource(Res.string.season_field_season_name)) },
            modifier = Modifier.width(fieldWidth),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    state.searchSeason()
                }) {
                    Icon(Icons.Default.Search, null)
                }
            },
        )
        // 动画季度搜索结果
        Box {
            DropdownMenu(
                expanded = state.animeSeasonSearchVisible.value,
                onDismissRequest = { state.animeSeasonSearchVisible.value = false },
                scrollState = rememberScrollState(),
                modifier = Modifier.width(fieldWidth).heightIn(max = fieldWidth)
            ) {
                state.animeSeasonSearchResult.value.forEach { season ->
                    DropdownMenuItem(
                        text = { Text("${season.season} - ${season.name} ${season.airDate}") },
                        onClick = {
                            state.selectSeason(season)
                        },
                    )
                }
                if (state.animeSeasonSearchResult.value.isEmpty()) {
                    Text(stringResource(Res.string.no_data), modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun rememberCreateAnimeSeasonState(
    service: AnimeSeasonService
): CreateAnimeSeasonState {
    return remember {
        CreateAnimeSeasonState(service)
    }
}

class CreateAnimeSeasonState(
    private val service: AnimeSeasonService
) {
    val isCreateAnime: MutableState<Boolean> = mutableStateOf(false)

    private val searchState = AnimeSeasonSearchState(service)

    val animeSearchVisible: MutableState<Boolean> get() = searchState.animeSearchVisible
    val animeNameSearch: MutableState<String> get() = searchState.animeNameSearch
    val animeSearchResult: MutableState<List<Anime>> get() = searchState.animeSearchResult

    val anime: MutableState<Anime?> get() = searchState.selectedAnime

    val animeSeason: MutableState<AnimeSeason?> get() = searchState.selectedAnimeSeason

    val animeSeasonSearchVisible: MutableState<Boolean> get() = searchState.animeSeasonSearchVisible
    val animeSeasonSearchResult: MutableState<List<AnimeSeason>> get() = searchState.animeSeasonSearchResult

    fun resetField() {
        searchState.reset()
    }

    fun reset() {
        isCreateAnime.value = false
        resetField()
    }

    fun searchAnime() {
        searchState.searchAnime(isCreateAnime.value)
    }

    fun selectAnime(selected: Anime) {
        searchState.selectAnime(selected)
    }

    fun searchSeason() {
        searchState.searchSeason(isCreateAnime.value)
    }

    fun selectSeason(season: AnimeSeason) {
        searchState.selectSeason(season)
    }

    fun save(
        onSuccess: (AnimeSeason) -> Unit,
        animeNameIsBlankMsg: String,
        animeSeasonIsBlankMsg: String
    ) {
        requireNotNull(anime.value) { animeNameIsBlankMsg }
        requireNotNull(
            animeSeason.value,
            lazyMessage = { animeSeasonIsBlankMsg })
            .let {
                service.saveAnimeSeason(it, onSuccess = { saved ->
                    // 若创建时未带全集数，且存在 TMDB，则补同步
                    if (saved.anime?.tmdbId != null || anime.value?.tmdbId != null) {
                        service.syncEpisodes(
                            seasonId = saved.id,
                            onSuccess = { onSuccess(saved) },
                            onError = { onSuccess(saved) }
                        )
                    } else {
                        onSuccess(saved)
                    }
                })
            }
    }
}