package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.enums.AnimeString
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.navigation.AcgnNavigationRoute
import dev.sunriseydy.acgn.client.onSuccessData
import dev.sunriseydy.acgn.client.utils.RequiredFieldLabel
import kotlinx.coroutines.launch

/**
 * @author SunriseYDY
 * @date 2024-08-15 16:08
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AnimeSeason(appState: AppState) {
    val sectionMapState: MutableState<MutableMap<String, List<AnimeSeason>>> =
        remember { mutableStateOf(mutableMapOf()) }
    val state: LazyStaggeredGridState = rememberLazyStaggeredGridState()
    val init = remember { mutableStateOf(false) }
    val loading = remember { mutableStateOf(false) }
    val createDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val operator = AnimeSeasonOperator(appState)

    fun loadData() {
        if (!loading.value && !init.value) {
            loading.value = true
            operator.loadData(onSuccess = {
                sectionMapState.value = it
                init.value = true
                loading.value = false
            }, onError = { loading.value = false })
        }
    }

    // 加载数据
    loadData()

    // 渲染组件
    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(AcgnNavigationRoute.ANIME_SEASON.localization) {
            IconButton(onClick = {
                init.value = false
                loadData()
            }) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = {
                createDialogVisible.value = true
            }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp))
            }
        }
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(8.dp),
        ) {
            sectionMapState.value.forEach { sectionMap ->
                item(span = StaggeredGridItemSpan.FullLine) {
                    PageTitle(sectionMap.key)
                }
                items(sectionMap.value) { season ->
                    Card(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                            SelectionContainer {
                                Text(text = season.name, style = MaterialTheme.typography.titleLarge)
                            }
                            Text(text = season.description ?: "", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
    // 创建动画季度弹窗
    CreateAnimeSeason(operator, createDialogVisible)
}

@Composable
private fun CreateAnimeSeason(
    operator: AnimeSeasonOperator,
    createDialogVisible: MutableState<Boolean>,
    onSuccess: () -> Unit = { },
) {
    val isCreateAnime = remember { mutableStateOf(false) }

    val animeSearchVisible = remember { mutableStateOf(false) }
    val animeNameSearch: MutableState<String?> = remember { mutableStateOf(null) }
    val animeSearchResult: MutableState<Map<ULong, String>> = remember { mutableStateOf(emptyMap()) }

    val anime: MutableState<Anime?> = remember { mutableStateOf(null) }

    val animeSeason: MutableState<AnimeSeason?> = remember { mutableStateOf(null) }

    val animeSeasonSearchVisible = remember { mutableStateOf(false) }
    val animeSeasonSearchResult: MutableState<List<AnimeSeason>> = remember { mutableStateOf(emptyList()) }

    fun closeCreateDialog() {
        createDialogVisible.value = false
        isCreateAnime.value = false

        animeSearchVisible.value = false
        animeNameSearch.value = null
        animeSearchResult.value = emptyMap()

        anime.value = null

        animeSeason.value = null

        animeSeasonSearchVisible.value = false
        animeSeasonSearchResult.value = emptyList()
    }

    FormDialog(
        formDialogVisible = createDialogVisible,
        onDismissRequest = { closeCreateDialog() },
        onConfirmation = {
            println(anime.value)
            println(animeSeason.value)
            onSuccess()
        },
    ) {
        val fieldWidth = 300.dp
        Row {
            Text(
                AnimeString.SEASON_FIELD_IS_CREATE_ANIME.localization,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Checkbox(
                checked = isCreateAnime.value,
                onCheckedChange = { isCreateAnime.value = it }
            )
        }

        // 动画名称搜索框
        OutlinedTextField(
            value = animeNameSearch.value ?: "",
            onValueChange = { animeNameSearch.value = it },
            modifier = Modifier.width(fieldWidth),
            label = {
                RequiredFieldLabel(
                    AnimeString.SEASON_FIELD_ANIME_NAME_SEARCH.localization +
                            if (isCreateAnime.value) AnimeString.SEARCH_TMDB.localization else AnimeString.SEARCH_LOCAL.localization
                )
            },
            leadingIcon = {
                IconButton(onClick = {
                    operator.searchAnime(name = animeNameSearch.value ?: "") {
                        animeSearchResult.value = it
                    }
                    animeSearchVisible.value = true
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
                animeSearchResult.value.map { animeMap ->
                    DropdownMenuItem(
                        text = { Text(animeMap.value) },
                        onClick = {
                            operator.getAnimeById(animeMap.key) {
                                anime.value = it
                                animeNameSearch.value = it.name
                                animeSearchVisible.value = false
                                animeSearchResult.value = emptyMap()
                            }
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
            value = if (animeSeason.value == null) "" else "${animeSeason.value?.season ?: ""} - ${animeSeason.value?.name ?: ""}",
            onValueChange = { },
            label = { RequiredFieldLabel(AnimeString.SEASON_FIELD_SEASON_NAME.localization) },
            modifier = Modifier.width(fieldWidth),
            readOnly = true,
            leadingIcon = {
                IconButton(onClick = {
                    if (isCreateAnime.value && anime.value != null && anime.value!!.animeSeasons.isNotEmpty()) {
                        // 如果创建动画，则直接取tmdb动画搜索结果的季度
                        animeSeasonSearchResult.value = anime.value!!.animeSeasons
                        animeSeasonSearchVisible.value = true
                    } else if (anime.value != null && anime.value?.tmdbId != null) {
                        // 否则如果动画有tmdbId，则取tmdb动画季度
                        operator.getAnimeByTmdbId(anime.value!!.tmdbId!!) {
                            animeSeasonSearchResult.value = it.animeSeasons
                            animeSeasonSearchVisible.value = true
                        }
                    }
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
                animeSeasonSearchResult.value.map { it ->
                    DropdownMenuItem(
                        text = { Text(it.season.toString() + it.name) },
                        onClick = {
                            animeSeason.value = it
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

private class AnimeSeasonOperator(
    val appState: AppState,
) {
    fun loadData(onSuccess: (MutableMap<String, List<AnimeSeason>>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.getAnimeSeasonSectionMap().onSuccessData(appState, onSuccess, onError)
        }
    }

    fun searchAnime(name: String, onSuccess: (Map<ULong, String>) -> Unit) {
        appState.scope.launch {
            appState.api.anime.getAnimeNameAndId(name).onSuccessData(appState, onSuccess)
        }
    }

    fun searchAnimeFromTMDB(name: String, onSuccess: (List<Anime>) -> Unit) {
        appState.scope.launch {
            appState.api.anime.searchTmdbAnimeTv(name).onSuccessData(appState, onSuccess)
        }
    }

    fun getAnimeByTmdbId(id: ULong, onSuccess: (Anime) -> Unit) {
        appState.scope.launch {
            appState.api.anime.getTmdbAnimeTvDetail(id).onSuccessData(appState, onSuccess)
        }
    }

    fun getAnimeById(id: ULong, onSuccess: (Anime) -> Unit) {
        appState.scope.launch {
            appState.api.anime.getAnimeById(id).onSuccessData(appState, onSuccess)
        }
    }
}