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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Edit
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
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.enums.AnimeString
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.AcgnAlertDialog
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.navigation.AcgnNavigationRoute
import dev.sunriseydy.acgn.client.onSuccess
import dev.sunriseydy.acgn.client.onSuccessData
import dev.sunriseydy.acgn.client.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.utils.RequiredSupportingText
import kotlinx.coroutines.launch
import kotlin.collections.List

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
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val handleFileDialogVisible = remember { mutableStateOf(false) }
    val filePath = remember { mutableStateOf("") }
    val isDeleteSource = remember { mutableStateOf(false) }
    val isDeleteTarget = remember { mutableStateOf(false) }
    val currentSeason: MutableState<AnimeSeason?> = remember { mutableStateOf(null) }
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

    fun openHandleFileDialog() {
        handleFileDialogVisible.value = true
        filePath.value = ""
        isDeleteSource.value = false
        isDeleteTarget.value = false
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
                    val buttonGroupVisible = remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        onClick = { buttonGroupVisible.value = !buttonGroupVisible.value }
                    ) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                            SelectionContainer {
                                Text(text = season.name, style = MaterialTheme.typography.titleLarge)
                            }
                            Text(text = season.description ?: "", style = MaterialTheme.typography.bodyLarge)
                            if (buttonGroupVisible.value) {
                                Row {
                                    IconButton(onClick = {
                                        currentSeason.value = season
                                        deleteDialogVisible.value = true
                                    }) {
                                        Icon(Icons.Default.Delete, null)
                                    }
                                    IconButton(onClick = {
                                        currentSeason.value = season
                                        createDialogVisible.value = true
                                    }) {
                                        Icon(Icons.Default.Edit, null)
                                    }
                                    IconButton(onClick = {
                                        currentSeason.value = season
                                        openHandleFileDialog()
                                    }) {
                                        Icon(Icons.Default.DriveFolderUpload, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // 创建动画季度弹窗
    CreateAnimeSeason(operator, createDialogVisible, onConfirmation = { animeSeason ->
        println(animeSeason)
        var errorMessage: String? = null
        operator.saveAnimeSeason(currentSeason.value?.let { animeSeason.copy(id = it.id) } ?: animeSeason,
            onError = { errorMessage = it })
        errorMessage
    })
    // 删除动画季度弹窗
    AcgnAlertDialog(
        alertDialogVisible = deleteDialogVisible,
        onConfirmation = {
            currentSeason.value?.also {
                operator.deleteSeason(it.id)
                currentSeason.value = null
                deleteDialogVisible.value = false
                loadData()
            }
        },
        dialogTitle = CommonString.DELETE.localization + currentSeason.value?.name,
    )
    // 处理文件弹窗
    FormDialog(
        formDialogVisible = handleFileDialogVisible,
        onConfirmation = {
            currentSeason.value?.let {
                operator.handleAnimeSeasonFile(
                    AnimeSeasonFile(
                        id = it.id,
                        path = filePath.value,
                        isDeleteSource = isDeleteSource.value,
                        isDeleteTarget = isDeleteTarget.value,
                    ),
                    onSuccess = { handleFileDialogVisible.value = false },
                    onError = { throw error(it) }
                )
            }
        },
    ) {
        OutlinedTextField(
            value = filePath.value,
            onValueChange = { filePath.value = it },
            label = { RequiredFieldLabel(CommonString.FILE_PATH.localization) },
            supportingText = { RequiredSupportingText(filePath, CommonString.FILE_PATH.localization) }
        )
        Row {
            Text(
                CommonString.DELETE_SOURCE.localization,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Checkbox(
                checked = isDeleteSource.value,
                onCheckedChange = { isDeleteSource.value = it },
            )
        }
        Row {
            Text(
                CommonString.DELETE_TARGET.localization,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Checkbox(
                checked = isDeleteTarget.value,
                onCheckedChange = { isDeleteTarget.value = it },
            )
        }
    }
}

@Composable
private fun CreateAnimeSeason(
    operator: AnimeSeasonOperator,
    createDialogVisible: MutableState<Boolean>,
    onConfirmation: (AnimeSeason) -> String? = { animeSeason -> null },
) {
    val isCreateAnime = remember { mutableStateOf(false) }

    val animeSearchVisible = remember { mutableStateOf(false) }
    val animeNameSearch: MutableState<String> = remember { mutableStateOf("") }
    val animeSearchResult: MutableState<List<Anime>> = remember { mutableStateOf(emptyList()) }

    val anime: MutableState<Anime?> = remember { mutableStateOf(null) }

    val animeSeason: MutableState<AnimeSeason?> = remember { mutableStateOf(null) }

    val animeSeasonSearchVisible = remember { mutableStateOf(false) }
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

    FormDialog(
        formDialogVisible = createDialogVisible,
        onDismissRequest = { closeCreateDialog() },
        onConfirmation = {
            requireNotNull(anime.value) { AnimeString.SEASON_FIELD_ANIME_NAME.localization + CommonString.IS_BLANK.localization }
            requireNotNull(animeSeason.value,
                lazyMessage = { AnimeString.SEASON_FIELD_SEASON_NAME.localization + CommonString.IS_BLANK.localization })
                .let {
                    onConfirmation(it)
                    closeCreateDialog()
                }
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
                    if (animeNameSearch.value.isNotBlank()) {
                        if (isCreateAnime.value) {
                            operator.searchAnimeFromTMDB(name = animeNameSearch.value, onSuccess = {
                                animeSearchResult.value = it
                                animeSearchVisible.value = true
                            })
                        } else {
                            operator.searchAnime(name = animeNameSearch.value, onSuccess = {
                                animeSearchResult.value = it
                                animeSearchVisible.value = true
                            })
                        }
                    }
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
            value = if (animeSeason.value == null) "" else "${animeSeason.value?.season ?: ""} - ${animeSeason.value?.name ?: ""}",
            onValueChange = { },
            label = { RequiredFieldLabel(AnimeString.SEASON_FIELD_SEASON_NAME.localization) },
            modifier = Modifier.width(fieldWidth),
            readOnly = true,
            leadingIcon = {
                IconButton(onClick = {
                    anime.value?.let { anime ->
                        if (isCreateAnime.value && anime.animeSeasons.isNotEmpty()) {
                            // 如果创建动画，则直接取tmdb动画搜索结果的季度
                            animeSeasonSearchResult.value = anime.animeSeasons
                            animeSeasonSearchVisible.value = true
                        } else {
                            anime.tmdbId?.let {
                                // 否则如果动画有tmdbId，则取tmdb动画季度
                                operator.getAnimeByTmdbId(it, onSuccess = {
                                    animeSeasonSearchResult.value = it.animeSeasons
                                    animeSeasonSearchVisible.value = true
                                }, onError = { throw error(it) })
                            }
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

private class AnimeSeasonOperator(
    val appState: AppState,
) {
    fun loadData(onSuccess: (MutableMap<String, List<AnimeSeason>>) -> Unit, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.getAnimeSeasonSectionMap().onSuccessData(appState, onSuccess, onError)
        }
    }

    fun saveAnimeSeason(animeSeason: AnimeSeason, onSuccess: () -> Unit = { }, onError: (String) -> Unit = { }) {
        appState.scope.launch {
            appState.api.anime.saveAnimeSeason(animeSeason).onSuccess(appState, onSuccess, onError)
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
}