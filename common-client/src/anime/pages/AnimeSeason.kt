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
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.components.CreateAnimeSeason
import dev.sunriseydy.acgn.client.anime.components.SearchBgmAnimeSeason
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.AlertDialog
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.base.components.PageTitle
import dev.sunriseydy.acgn.client.base.navigation.TopLevelRouteEnum
import dev.sunriseydy.acgn.client.base.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.base.utils.RequiredSupportingText
import dev.sunriseydy.acgn.client.res.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.resources.stringResource

private val logger = KotlinLogging.logger { }

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
    val loading = remember { mutableStateOf(false) }
    val createDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val handleFileDialogVisible = remember { mutableStateOf(false) }
    val filePath = remember { mutableStateOf("") }
    val isDeleteSource = remember { mutableStateOf(false) }
    val isDeleteTarget = remember { mutableStateOf(false) }

    val searchBgmDialogVisible = remember { mutableStateOf(false) }
    val currentSeason: MutableState<AnimeSeason?> = remember { mutableStateOf(null) }
    // Initialize service
    val animeSeasonService = remember(appState) { AnimeSeasonService(appState) }

    fun loadData() {
        if (!loading.value) {
            loading.value = true
            animeSeasonService.loadData(
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
    
    // Initial load
    LaunchedEffect(Unit) {
        loadData()
    }

    fun openHandleFileDialog() {
        handleFileDialogVisible.value = true
        filePath.value = ""
        isDeleteSource.value = false
        isDeleteTarget.value = false
    }

    // UI Structure
    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(TopLevelRouteEnum.ANIME_SEASON.meaning) {
            IconButton(onClick = {
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
                    // 动画季度列表
                    val buttonGroupVisible = remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        onClick = { buttonGroupVisible.value = !buttonGroupVisible.value }
                    ) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                            SelectionContainer {
                                Text(
                                    text = "${season.anime?.name ?: ""} - ${season.name}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Text(
                                text = "第 ${season.season} 季 共 ${season.numberOfEpisodes} 集",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = season.description?.takeUnless { it.isBlank() }
                                    ?: season.anime?.description.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge
                            )
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
                                        openHandleFileDialog()
                                    }) {
                                        Icon(Icons.Default.DriveFolderUpload, null)
                                    }
                                    // Search BGM
                                    IconButton(onClick = {
                                        currentSeason.value = season
                                        searchBgmDialogVisible.value = true
                                    }) {
                                        Icon(Icons.Default.Search, null)
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
    CreateAnimeSeason(animeSeasonService, createDialogVisible, onSuccess = { loadData() })
    // 删除动画季度弹窗
    AlertDialog(
        alertDialogVisible = deleteDialogVisible,
        onConfirmation = {
            currentSeason.value?.also {
                animeSeasonService.deleteSeason(it.id) {
                    currentSeason.value = null
                    deleteDialogVisible.value = false
                    loadData()
                }
            }
        },
        dialogTitle = stringResource(Res.string.delete) + (currentSeason.value?.name ?: ""),
    )
    // 处理文件弹窗
    val errorMessage = mutableStateOf<String?>(null)
    FormDialog(
        formDialogVisible = handleFileDialogVisible,
        onConfirmation = {
            currentSeason.value?.let {
                animeSeasonService.handleAnimeSeasonFile(
                    AnimeSeasonFile(
                        id = it.id,
                        path = filePath.value,
                        isDeleteSource = isDeleteSource.value,
                        isDeleteTarget = isDeleteTarget.value,
                    ),
                    onSuccess = { handleFileDialogVisible.value = false },
                    onError = { errorMessage.value = it },
                )
            }
        },
        errorMessage = errorMessage
    ) {
        OutlinedTextField(
            value = filePath.value,
            onValueChange = { filePath.value = it },
            label = { RequiredFieldLabel(stringResource(Res.string.file_path)) },
            supportingText = { RequiredSupportingText(filePath, stringResource(Res.string.file_path)) }
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

    // Search BGM
    SearchBgmAnimeSeason(
        animeSeasonService = animeSeasonService,
        visible = searchBgmDialogVisible,
        currentSeason = currentSeason.value,
        onSuccess = { loadData() }
    )
}