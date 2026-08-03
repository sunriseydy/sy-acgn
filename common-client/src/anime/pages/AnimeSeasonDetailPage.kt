package dev.sunriseydy.acgn.client.anime.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.components.SearchBgmAnimeSeason
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.AlertDialog
import dev.sunriseydy.acgn.client.base.components.AttachImage
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.base.components.PageTitle
import dev.sunriseydy.acgn.client.base.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.base.utils.RequiredSupportingText
import dev.sunriseydy.acgn.client.res.*
import dev.sunriseydy.acgn.tools.i
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * 动画季度详情页：展示季度信息与集数列表。
 */
@Composable
fun AnimeSeasonDetailPage(appState: AppState, seasonId: ULong) {
    val animeSeasonService = remember(appState) { AnimeSeasonService(appState) }
    val seasonState = remember { mutableStateOf<AnimeSeason?>(null) }
    val episodesState = remember { mutableStateOf<List<AnimeEpisode>>(emptyList()) }
    val loading = remember { mutableStateOf(false) }
    val episodesLoading = remember { mutableStateOf(false) }

    val deleteDialogVisible = remember { mutableStateOf(false) }
    val handleFileDialogVisible = remember { mutableStateOf(false) }
    val searchBgmDialogVisible = remember { mutableStateOf(false) }
    val filePath = remember { mutableStateOf("") }
    val isDeleteSource = remember { mutableStateOf(false) }
    val isDeleteTarget = remember { mutableStateOf(false) }
    val episodeOffset = remember { mutableStateOf("") }
    val fileErrorMessage = remember { mutableStateOf<String?>(null) }

    fun loadEpisodes() {
        episodesLoading.value = true
        animeSeasonService.loadEpisodes(
            seasonId = seasonId,
            onSuccess = {
                episodesState.value = it
                episodesLoading.value = false
            },
            onError = {
                episodesLoading.value = false
                appState.scope.launch {
                    appState.snackbarHostState.showSnackbar(it)
                }
            }
        )
    }

    fun loadDetail() {
        if (loading.value) return
        loading.value = true
        animeSeasonService.loadSeasonById(
            seasonId = seasonId,
            onSuccess = { season ->
                seasonState.value = season
                // 优先使用详情接口附带的集数，避免二次请求；为空时再单独拉
                if (season.animeEpisodes.isNotEmpty()) {
                    episodesState.value = season.animeEpisodes
                    episodesLoading.value = false
                } else {
                    loadEpisodes()
                }
                loading.value = false
            },
            onError = {
                loading.value = false
                appState.scope.launch {
                    appState.snackbarHostState.showSnackbar(it)
                }
            }
        )
    }

    LaunchedEffect(seasonId) {
        loadDetail()
    }

    fun openHandleFileDialog() {
        handleFileDialogVisible.value = true
        filePath.value = ""
        isDeleteSource.value = false
        isDeleteTarget.value = false
        episodeOffset.value = ""
        fileErrorMessage.value = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val season = seasonState.value
        PageTitle(
            title = season?.let { "${it.anime?.name ?: ""} - ${it.name}".trim().trimStart('-').trim() }
                ?: "动画季度详情"
        ) {
            IconButton(onClick = { appState.navigationAction.removeLast() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            IconButton(onClick = { loadDetail() }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh))
            }
            if (season?.anime?.tmdbId != null) {
                IconButton(onClick = {
                    animeSeasonService.refreshTmdbData(
                        season = season,
                        onSuccess = {
                            loadDetail()
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
            IconButton(onClick = { searchBgmDialogVisible.value = true }) {
                Icon(Icons.Default.Search, contentDescription = "关联 Bangumi")
            }
            IconButton(onClick = { openHandleFileDialog() }) {
                Icon(Icons.Default.DriveFolderUpload, contentDescription = "处理文件")
            }
            IconButton(onClick = { deleteDialogVisible.value = true }) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
            }
        }

        if (loading.value && season == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (season == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("动画季度不存在")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val posterId = season.posterId
                            if (!posterId.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    AttachImage(
                                        appState = appState,
                                        attachId = posterId,
                                        modifier = Modifier.width(300.dp).height(400.dp),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                            SelectionContainer {
                                Text(
                                    text = "${season.anime?.name ?: ""} - ${season.name}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("第 ${season.season} 季") }
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text("共 ${season.numberOfEpisodes} 集") }
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text("已同步 ${episodesState.value.size} 集") }
                                )
                                AnimeAdditionType.FileStatus.additionalInfo(season.additions)?.also {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(i(it.additionalValue)) }
                                    )
                                }
                            }
                            season.airDate?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "播出：${it}（${season.year} / ${season.month} 月）",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val desc = season.description?.takeUnless { it.isBlank() }
                                ?: season.anime?.description.orEmpty()
                            if (desc.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(desc, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "集数列表",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                when {
                    episodesLoading.value -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    episodesState.value.isEmpty() -> {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (season.anime?.tmdbId != null) {
                                            "暂无集数，可点击同步按钮从 TMDB 拉取"
                                        } else {
                                            "暂无集数"
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        items(episodesState.value) { episode ->
                            EpisodeDetailItem(episode = episode)
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        alertDialogVisible = deleteDialogVisible,
        onConfirmation = {
            animeSeasonService.deleteSeason(seasonId) {
                deleteDialogVisible.value = false
                appState.navigationAction.removeLast()
            }
        },
        dialogTitle = stringResource(Res.string.delete) + (seasonState.value?.name ?: ""),
    )

    FormDialog(
        formDialogVisible = handleFileDialogVisible,
        onConfirmation = {
            animeSeasonService.handleAnimeSeasonFile(
                AnimeSeasonFile(
                    id = seasonId,
                    path = filePath.value,
                    isDeleteSource = isDeleteSource.value,
                    isDeleteTarget = isDeleteTarget.value,
                    episodeOffset = episodeOffset.value.toIntOrNull() ?: 0,
                ),
                onSuccess = {
                    handleFileDialogVisible.value = false
                    loadDetail()
                },
                onError = { fileErrorMessage.value = it },
            )
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
        currentSeason = seasonState.value,
        onSuccess = { loadDetail() }
    )
}

@Composable
private fun EpisodeDetailItem(episode: AnimeEpisode) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "E${episode.episode.toString().padStart(2, '0')}  ${episode.name}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                episode.airDate?.let {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            episode.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
