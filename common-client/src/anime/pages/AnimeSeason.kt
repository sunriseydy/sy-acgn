package dev.sunriseydy.acgn.client.anime.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.components.CreateAnimeSeason
import dev.sunriseydy.acgn.client.anime.components.SearchBgmAnimeSeason
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.AlertDialog
import dev.sunriseydy.acgn.client.base.components.AttachImage
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.base.components.PageTitle
import dev.sunriseydy.acgn.client.base.navigation.AnimeSeasonDetailRoute
import dev.sunriseydy.acgn.client.base.navigation.TopLevelRouteEnum
import dev.sunriseydy.acgn.client.base.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.base.utils.RequiredSupportingText
import dev.sunriseydy.acgn.client.res.*
import dev.sunriseydy.acgn.tools.i
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val logger = KotlinLogging.logger { }

/**
 * 动画季度列表页
 *
 * @author SunriseYDY
 * @date 2024-08-15 16:08
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AnimeSeason(appState: AppState) {
    val sectionMapState: MutableState<MutableMap<String, List<AnimeSeason>>> =
        remember { mutableStateOf(mutableMapOf()) }
    val state: LazyStaggeredGridState = rememberLazyStaggeredGridState()
    val loading = remember { mutableStateOf(false) }
    val createDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val searchName = remember { mutableStateOf("") }
    val animeSeasonService = remember(appState) { AnimeSeasonService(appState) }

    // 卡片操作（同步 TMDB / 关联 Bangumi / 处理文件 / 删除）对应的目标季度与弹窗状态
    val actionSeason = remember { mutableStateOf<AnimeSeason?>(null) }
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val handleFileDialogVisible = remember { mutableStateOf(false) }
    val searchBgmDialogVisible = remember { mutableStateOf(false) }
    val filePath = remember { mutableStateOf("") }
    val isDeleteSource = remember { mutableStateOf(false) }
    val isDeleteTarget = remember { mutableStateOf(false) }
    val episodeOffset = remember { mutableStateOf("") }
    val fileErrorMessage = remember { mutableStateOf<String?>(null) }

    fun loadData(fromDb: Boolean = false) {
        if (!loading.value) {
            loading.value = true
            animeSeasonService.loadData(
                name = searchName.value,
                fromDb = fromDb,
                onSuccess = {
                    sectionMapState.value = it
                    loading.value = false
                },
                onError = {
                    loading.value = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    fun openHandleFileDialog(season: AnimeSeason) {
        actionSeason.value = season
        handleFileDialogVisible.value = true
        filePath.value = ""
        isDeleteSource.value = false
        isDeleteTarget.value = false
        episodeOffset.value = ""
        fileErrorMessage.value = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(TopLevelRouteEnum.ANIME_SEASON.meaning) {
            OutlinedTextField(
                value = searchName.value,
                onValueChange = { searchName.value = it },
                label = { Text(stringResource(Res.string.search)) },
                singleLine = true,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = {
                loadData()
            }) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = {
                searchName.value = ""
                loadData(fromDb = true)
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
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(8.dp),
        ) {
            sectionMapState.value.forEach { sectionMap ->
                val (title, seasons) = sectionMap
                item(span = StaggeredGridItemSpan.FullLine) {
                    PageTitle(title)
                }
                items(seasons) { season ->
                    Card(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        onClick = {
                            appState.navigationAction.add(AnimeSeasonDetailRoute(season.id))
                        }
                    ) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val posterId = season.posterId
                                if (!posterId.isNullOrBlank()) {
                                    AttachImage(
                                        appState = appState,
                                        attachId = posterId,
                                        modifier = Modifier.padding(end = 8.dp).width(300.dp).height(400.dp),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                            SelectionContainer {
                                Text(
                                    text = "${season.anime?.name ?: ""} - ${season.name}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("第 ${season.season} 季") }
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text("共 ${season.numberOfEpisodes} 集") }
                                )
                                AnimeAdditionType.FileStatus.additionalInfo(season.additions)?.also {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(i(it.additionalValue)) }
                                    )
                                }
                            }
                            Text(
                                text = season.description?.takeUnless { it.isBlank() }
                                    ?: season.anime?.description.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (season.anime?.tmdbId != null) {
                                    IconButton(onClick = {
                                        animeSeasonService.refreshTmdbData(
                                            season = season,
                                            onSuccess = {
                                                loadData()
                                                appState.scope.launch {
                                                    appState.snackbarHostState.showSnackbar("已从 TMDB 同步季度与集数")
                                                }
                                            },
                                            onError = { errorMsg ->
                                                appState.scope.launch {
                                                    appState.snackbarHostState.showSnackbar(errorMsg)
                                                }
                                            }
                                        )
                                    }) {
                                        Icon(Icons.Default.Sync, contentDescription = "同步 TMDB")
                                    }
                                }
                                IconButton(onClick = {
                                    actionSeason.value = season
                                    searchBgmDialogVisible.value = true
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "关联 Bangumi")
                                }
                                IconButton(onClick = { openHandleFileDialog(season) }) {
                                    Icon(Icons.Default.DriveFolderUpload, contentDescription = "处理文件")
                                }
                                IconButton(onClick = {
                                    actionSeason.value = season
                                    deleteDialogVisible.value = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    CreateAnimeSeason(animeSeasonService, createDialogVisible, onSuccess = { loadData() })

    AlertDialog(
        alertDialogVisible = deleteDialogVisible,
        onConfirmation = {
            actionSeason.value?.let { season ->
                animeSeasonService.deleteSeason(season.id) {
                    deleteDialogVisible.value = false
                    loadData()
                }
            }
        },
        dialogTitle = stringResource(Res.string.delete) + (actionSeason.value?.name ?: ""),
    )

    FormDialog(
        formDialogVisible = handleFileDialogVisible,
        onConfirmation = {
            actionSeason.value?.let { season ->
                animeSeasonService.handleAnimeSeasonFile(
                    AnimeSeasonFile(
                        id = season.id,
                        path = filePath.value,
                        isDeleteSource = isDeleteSource.value,
                        isDeleteTarget = isDeleteTarget.value,
                        episodeOffset = episodeOffset.value.toIntOrNull() ?: 0,
                    ),
                    onSuccess = {
                        handleFileDialogVisible.value = false
                        loadData()
                    },
                    onError = { fileErrorMessage.value = it },
                )
            }
        },
        errorMessage = fileErrorMessage
    ) {
        OutlinedTextField(
            value = filePath.value,
            onValueChange = { filePath.value = it },
            label = { RequiredFieldLabel(stringResource(Res.string.file_path)) },
            supportingText = { RequiredSupportingText(filePath, stringResource(Res.string.file_path)) }
        )
        OutlinedTextField(
            value = episodeOffset.value,
            onValueChange = { episodeOffset.value = it },
            label = { Text(stringResource(Res.string.episode_offset)) },
            supportingText = { Text(stringResource(Res.string.episode_offset_supporting_text)) }
        )
        Row {
            Text(
                stringResource(Res.string.delete_source),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Checkbox(
                checked = isDeleteSource.value,
                onCheckedChange = { isDeleteSource.value = it },
            )
        }
        Row {
            Text(
                stringResource(Res.string.delete_target),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Checkbox(
                checked = isDeleteTarget.value,
                onCheckedChange = { isDeleteTarget.value = it },
            )
        }
    }

    SearchBgmAnimeSeason(
        animeSeasonService = animeSeasonService,
        visible = searchBgmDialogVisible,
        currentSeason = actionSeason.value,
        onSuccess = { loadData() }
    )
}
