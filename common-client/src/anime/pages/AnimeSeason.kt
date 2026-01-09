package dev.sunriseydy.acgn.client.anime.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.components.CreateAnimeSeason
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.AcgnAlertDialog
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.navigation.TopLevelRouteEnum
import dev.sunriseydy.acgn.client.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.utils.RequiredSupportingText
import io.github.oshai.kotlinlogging.KotlinLogging

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
    val init = remember { mutableStateOf(false) }
    val loading = remember { mutableStateOf(false) }
    val createDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val handleFileDialogVisible = remember { mutableStateOf(false) }
    val filePath = remember { mutableStateOf("") }
    val isDeleteSource = remember { mutableStateOf(false) }
    val isDeleteTarget = remember { mutableStateOf(false) }
    val currentSeason: MutableState<AnimeSeason?> = remember { mutableStateOf(null) }
    val animeSeasonService = AnimeSeasonService(appState)

    fun loadData(force: Boolean = false) {
        if (force || !loading.value && !init.value) {
            loading.value = true
            animeSeasonService.loadData(onSuccess = {
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
        PageTitle(TopLevelRouteEnum.ANIME_SEASON.meaning) {
            IconButton(onClick = {
                loadData(true)
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
                    // 动画季度列表
                    val buttonGroupVisible = remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        onClick = { buttonGroupVisible.value = !buttonGroupVisible.value }
                    ) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                            SelectionContainer {
                                Text(
                                    text = "${season.anime!!.name} - ${season.name}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Text(
                                text = if (season.description.isNullOrBlank()) {
                                    if (season.anime?.description.isNullOrBlank()) {
                                        ""
                                    } else {
                                        season.anime!!.description!!
                                    }
                                } else {
                                    season.description!!
                                },
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
    CreateAnimeSeason(animeSeasonService, createDialogVisible, onSuccess = { loadData(true) })
    // 删除动画季度弹窗
    AcgnAlertDialog(
        alertDialogVisible = deleteDialogVisible,
        onConfirmation = {
            currentSeason.value?.also {
                animeSeasonService.deleteSeason(it.id)
                currentSeason.value = null
                deleteDialogVisible.value = false
                loadData(true)
            }
        },
        dialogTitle = CommonString.DELETE.meaning + currentSeason.value?.name,
    )
    // 处理文件弹窗
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
                )
            }
        },
    ) {
        OutlinedTextField(
            value = filePath.value,
            onValueChange = { filePath.value = it },
            label = { RequiredFieldLabel(CommonString.FILE_PATH.meaning) },
            supportingText = { RequiredSupportingText(filePath, CommonString.FILE_PATH.meaning) }
        )
        Row {
            Text(
                CommonString.DELETE_SOURCE.meaning,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Checkbox(
                checked = isDeleteSource.value,
                onCheckedChange = { isDeleteSource.value = it },
            )
        }
        Row {
            Text(
                CommonString.DELETE_TARGET.meaning,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Checkbox(
                checked = isDeleteTarget.value,
                onCheckedChange = { isDeleteTarget.value = it },
            )
        }
    }
}