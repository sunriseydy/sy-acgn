package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.base.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.base.utils.RequiredSupportingText
import dev.sunriseydy.acgn.client.res.*
import kotlinx.coroutines.Job
import org.jetbrains.compose.resources.stringResource

/**
 * 从下载目录通过 AI 识别动画名并处理文件 / 创建动画季度的弹窗组件
 *
 * @author SunriseYDY
 * @date 2026-10-17 21:54
 */
@Composable
fun CreateAnimeSeasonFromFile(
    animeSeasonService: AnimeSeasonService,
    fileCreateDialogVisible: MutableState<Boolean>,
    onSuccess: (AnimeSeason) -> Unit = { },
) {
    val state = rememberCreateAnimeSeasonFromFileState(animeSeasonService)
    val filePathIsBlank =
        stringResource(Res.string.file_path) + stringResource(Res.string.is_blank)
    val animeNameIsBlank =
        stringResource(Res.string.season_field_anime_name) + stringResource(Res.string.is_blank)
    val animeSeasonIsBlank =
        stringResource(Res.string.season_field_season_name) + stringResource(Res.string.is_blank)
    val aiBlankNameMessage = stringResource(Res.string.ai_extract_name_blank)
    val aiFailedMessage = stringResource(Res.string.ai_extract_failed)
    val extractAnimeName = {
        state.extractAnimeName(
            filePathIsBlankMessage = filePathIsBlank,
            blankNameMessage = aiBlankNameMessage,
            failedMessage = aiFailedMessage,
        )
    }

    FormDialog(
        formDialogVisible = fileCreateDialogVisible,
        errorMessage = state.errorMessage,
        onDismissRequest = {
            fileCreateDialogVisible.value = false
            state.reset()
        },
        onConfirmation = {
            state.submit(
                filePathIsBlankMessage = filePathIsBlank,
                animeNameIsBlankMessage = animeNameIsBlank,
                animeSeasonIsBlankMessage = animeSeasonIsBlank,
                onSuccess = {
                    fileCreateDialogVisible.value = false
                    state.reset()
                    onSuccess(it)
                },
            )
        },
    ) {
        val fieldWidth = 500.dp
        Column(
            modifier = Modifier
                .width(fieldWidth)
                .heightIn(max = 700.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.handle_file_from_dir_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // 文件路径输入框
            OutlinedTextField(
                value = state.filePath.value,
                onValueChange = { state.filePath.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { RequiredFieldLabel(stringResource(Res.string.file_path)) },
                supportingText = {
                    if (state.isAiExtracting.value) {
                        Text(
                            stringResource(Res.string.ai_identifying),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        RequiredSupportingText(state.filePath, stringResource(Res.string.file_path))
                    }
                },
                trailingIcon = {
                    if (state.isAiExtracting.value) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = extractAnimeName) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = stringResource(Res.string.ai_extract_anime_name),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { extractAnimeName() })
            )

            // 识别出的动画名称
            if (state.extractedAnimeName.value.isNotBlank() || state.isAiExtracting.value) {
                OutlinedTextField(
                    value = state.extractedAnimeName.value,
                    onValueChange = { state.extractedAnimeName.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.ai_extracted_name)) },
                    trailingIcon = {
                        IconButton(onClick = { state.searchLocalSeasons(state.extractedAnimeName.value) }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.search))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { state.searchLocalSeasons(state.extractedAnimeName.value) })
                )
            }

            // 本地已有季度选择与提示
            if (state.hasSearchedLocal.value) {
                if (state.matchedSeasons.value.isNotEmpty()) {
                    if (!state.isCreateMode.value) {
                        // 展示本地已有季度下拉选择
                        val selected = state.selectedLocalSeason.value
                        val seasonText = if (selected != null) {
                            "${selected.anime?.name ?: ""} - 第 ${selected.season} 季 ${selected.name}"
                        } else {
                            ""
                        }
                        OutlinedTextField(
                            value = seasonText,
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { RequiredFieldLabel(stringResource(Res.string.select_local_season)) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    state.localSeasonDropdownVisible.value = !state.localSeasonDropdownVisible.value
                                }) {
                                    Icon(Icons.Default.Search, null)
                                }
                            }
                        )
                        Box {
                            DropdownMenu(
                                expanded = state.localSeasonDropdownVisible.value,
                                onDismissRequest = { state.localSeasonDropdownVisible.value = false },
                                modifier = Modifier.width(fieldWidth).heightIn(max = 300.dp)
                            ) {
                                state.matchedSeasons.value.forEach { season ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${season.anime?.name ?: ""} - 第 ${season.season} 季 ${season.name} (${season.year}/${season.month})")
                                        },
                                        onClick = {
                                            state.selectedLocalSeason.value = season
                                            state.localSeasonDropdownVisible.value = false
                                        }
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                state.isCreateMode.value = true
                                state.animeNameSearch.value = state.extractedAnimeName.value
                                state.searchAnime()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(Res.string.switch_to_create_season))
                        }
                    } else {
                        // 用户切换到了创建模式，提供返回按钮
                        TextButton(
                            onClick = { state.isCreateMode.value = false },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Text(stringResource(Res.string.switch_to_matched_season))
                        }
                    }
                } else {
                    // 本地无匹配数据
                    Text(
                        text = stringResource(Res.string.no_matched_season_tip),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // 创建并处理模式的表单内容
            if (state.isCreateMode.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            state.isCreateAnime.value = !state.isCreateAnime.value
                            state.resetCreateFields()
                        }
                        .padding(end = 8.dp)
                ) {
                    Text(stringResource(Res.string.season_field_is_create_anime))
                    Checkbox(
                        checked = state.isCreateAnime.value,
                        onCheckedChange = {
                            state.isCreateAnime.value = it
                            state.resetCreateFields()
                        }
                    )
                }

                // 动画名称搜索框
                OutlinedTextField(
                    value = state.animeNameSearch.value,
                    onValueChange = { state.animeNameSearch.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        RequiredFieldLabel(
                            stringResource(Res.string.season_field_anime_name_search) +
                                    if (state.isCreateAnime.value) stringResource(Res.string.search_tmdb)
                                    else stringResource(Res.string.search_local)
                        )
                    },
                    supportingText = {
                        RequiredSupportingText(
                            state.animeNameSearch,
                            stringResource(Res.string.season_field_anime_name_search)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { state.searchAnime() }) {
                            Icon(Icons.Default.Search, null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { state.searchAnime() })
                )

                // 动画搜索结果下拉
                Box {
                    DropdownMenu(
                        expanded = state.animeSearchVisible.value,
                        onDismissRequest = { state.animeSearchVisible.value = false },
                        scrollState = rememberScrollState(),
                        modifier = Modifier.width(fieldWidth).heightIn(max = 300.dp)
                    ) {
                        state.animeSearchResult.value.forEach { anime ->
                            DropdownMenuItem(
                                text = { Text("${anime.name}  ${anime.firstAirDate ?: ""}") },
                                onClick = { state.selectAnime(anime) }
                            )
                        }
                        if (state.animeSearchResult.value.isEmpty()) {
                            Text(stringResource(Res.string.no_data), modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                // 动画季度选择框
                OutlinedTextField(
                    value = state.selectedAnimeSeason.value?.let { "${it.season} - ${it.name}" } ?: "",
                    onValueChange = { },
                    label = { RequiredFieldLabel(stringResource(Res.string.season_field_season_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { state.searchSeason() }) {
                            Icon(Icons.Default.Search, null)
                        }
                    }
                )

                // 动画季度搜索结果下拉
                Box {
                    DropdownMenu(
                        expanded = state.animeSeasonSearchVisible.value,
                        onDismissRequest = { state.animeSeasonSearchVisible.value = false },
                        scrollState = rememberScrollState(),
                        modifier = Modifier.width(fieldWidth).heightIn(max = 300.dp)
                    ) {
                        state.animeSeasonSearchResult.value.forEach { season ->
                            DropdownMenuItem(
                                text = { Text("${season.season} - ${season.name} ${season.airDate ?: ""}") },
                                onClick = { state.selectSeason(season) }
                            )
                        }
                        if (state.animeSeasonSearchResult.value.isEmpty()) {
                            Text(stringResource(Res.string.no_data), modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // 集数偏移
            OutlinedTextField(
                value = state.episodeOffset.value,
                onValueChange = { state.episodeOffset.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.episode_offset)) },
                supportingText = { Text(stringResource(Res.string.episode_offset_supporting_text)) }
            )

            // 删除源路径与目标路径开关
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.delete_source), modifier = Modifier.align(Alignment.CenterVertically))
                Checkbox(
                    checked = state.isDeleteSource.value,
                    onCheckedChange = { state.isDeleteSource.value = it }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(Res.string.delete_target), modifier = Modifier.align(Alignment.CenterVertically))
                Checkbox(
                    checked = state.isDeleteTarget.value,
                    onCheckedChange = { state.isDeleteTarget.value = it }
                )
            }
        }
    }
}

@Composable
fun rememberCreateAnimeSeasonFromFileState(
    service: AnimeSeasonService
): CreateAnimeSeasonFromFileState {
    return remember(service) {
        CreateAnimeSeasonFromFileState(service)
    }
}

class CreateAnimeSeasonFromFileState(
    private val service: AnimeSeasonService
) {
    val filePath: MutableState<String> = mutableStateOf("")
    val episodeOffset: MutableState<String> = mutableStateOf("")
    val isDeleteSource: MutableState<Boolean> = mutableStateOf(true)
    val isDeleteTarget: MutableState<Boolean> = mutableStateOf(false)

    val isAiExtracting: MutableState<Boolean> = mutableStateOf(false)
    val extractedAnimeName: MutableState<String> = mutableStateOf("")

    /** 进行中的 AI 识别任务，弹窗关闭时取消 */
    private var aiExtractJob: Job? = null

    // 本地匹配结果
    val hasSearchedLocal: MutableState<Boolean> = mutableStateOf(false)
    val matchedSeasons: MutableState<List<AnimeSeason>> = mutableStateOf(emptyList())
    val selectedLocalSeason: MutableState<AnimeSeason?> = mutableStateOf(null)
    val localSeasonDropdownVisible: MutableState<Boolean> = mutableStateOf(false)

    // 是否处于创建模式（无本地匹配或用户主动切换）
    val isCreateMode: MutableState<Boolean> = mutableStateOf(false)

    // 创建模式状态（搜索与选择逻辑复用共享状态）
    val isCreateAnime: MutableState<Boolean> = mutableStateOf(true)
    val animeNameSearch: MutableState<String> get() = searchState.animeNameSearch
    val animeSearchResult: MutableState<List<Anime>> get() = searchState.animeSearchResult
    val animeSearchVisible: MutableState<Boolean> get() = searchState.animeSearchVisible
    val selectedAnime: MutableState<Anime?> get() = searchState.selectedAnime
    val animeSeasonSearchResult: MutableState<List<AnimeSeason>> get() = searchState.animeSeasonSearchResult
    val animeSeasonSearchVisible: MutableState<Boolean> get() = searchState.animeSeasonSearchVisible
    val selectedAnimeSeason: MutableState<AnimeSeason?> get() = searchState.selectedAnimeSeason

    val errorMessage: MutableState<String?> = mutableStateOf(null)
    val isSubmitting: MutableState<Boolean> = mutableStateOf(false)

    private val searchState = AnimeSeasonSearchState(service)

    fun reset() {
        aiExtractJob?.cancel()
        aiExtractJob = null
        filePath.value = ""
        episodeOffset.value = ""
        isDeleteSource.value = true
        isDeleteTarget.value = false
        isAiExtracting.value = false
        extractedAnimeName.value = ""
        hasSearchedLocal.value = false
        matchedSeasons.value = emptyList()
        selectedLocalSeason.value = null
        localSeasonDropdownVisible.value = false
        isCreateMode.value = false
        isCreateAnime.value = true
        searchState.reset()
        errorMessage.value = null
        isSubmitting.value = false
    }

    fun resetCreateFields() {
        searchState.resetSelection()
    }

    fun extractAnimeName(
        filePathIsBlankMessage: String,
        blankNameMessage: String,
        failedMessage: String,
    ) {
        if (isAiExtracting.value) return
        if (filePath.value.isBlank()) {
            errorMessage.value = filePathIsBlankMessage
            return
        }
        errorMessage.value = null
        isAiExtracting.value = true
        extractedAnimeName.value = ""
        aiExtractJob = service.extractAnimeNameByAi(
            directoryPath = filePath.value,
            blankNameMessage = blankNameMessage,
            failedMessage = failedMessage,
            onDelta = { delta ->
                extractedAnimeName.value = delta
            },
            onComplete = { finalName ->
                isAiExtracting.value = false
                extractedAnimeName.value = finalName
                searchLocalSeasons(finalName)
            },
            onError = { error ->
                isAiExtracting.value = false
                errorMessage.value = error
            }
        )
    }

    fun searchLocalSeasons(query: String) {
        if (query.isBlank()) return
        errorMessage.value = null
        service.searchAnimeSeason(
            name = query,
            onSuccess = { list ->
                matchedSeasons.value = list
                hasSearchedLocal.value = true
                if (list.isNotEmpty()) {
                    isCreateMode.value = false
                    selectedLocalSeason.value = list.firstOrNull()
                } else {
                    isCreateMode.value = true
                    selectedLocalSeason.value = null
                    animeNameSearch.value = query
                    searchAnime()
                }
            },
            onError = {
                errorMessage.value = it
            }
        )
    }

    fun searchAnime() {
        searchState.searchAnime(isCreateAnime.value) { errorMessage.value = it }
    }

    fun selectAnime(selected: Anime) {
        searchState.selectAnime(selected)
        // 选中动画后自动搜索其季度
        searchSeason()
    }

    fun searchSeason() {
        searchState.searchSeason(isCreateAnime.value) { errorMessage.value = it }
    }

    fun selectSeason(season: AnimeSeason) {
        searchState.selectSeason(season)
    }

    fun submit(
        filePathIsBlankMessage: String,
        animeNameIsBlankMessage: String,
        animeSeasonIsBlankMessage: String,
        onSuccess: (AnimeSeason) -> Unit
    ) {
        if (filePath.value.isBlank()) {
            errorMessage.value = filePathIsBlankMessage
            return
        }

        if (!isCreateMode.value) {
            val season = selectedLocalSeason.value
            if (season == null) {
                errorMessage.value = animeSeasonIsBlankMessage
                return
            }
            if (isSubmitting.value) return
            isSubmitting.value = true
            errorMessage.value = null
            service.handleAnimeSeasonFile(
                AnimeSeasonFile(
                    id = season.id,
                    path = filePath.value,
                    isDeleteSource = isDeleteSource.value,
                    isDeleteTarget = isDeleteTarget.value,
                    episodeOffset = episodeOffset.value.toIntOrNull() ?: 0,
                ),
                onSuccess = {
                    isSubmitting.value = false
                    onSuccess(season)
                },
                onError = { err ->
                    isSubmitting.value = false
                    errorMessage.value = err
                }
            )
        } else {
            val anime = selectedAnime.value
            val season = selectedAnimeSeason.value
            if (anime == null) {
                errorMessage.value = animeNameIsBlankMessage
                return
            }
            if (season == null) {
                errorMessage.value = animeSeasonIsBlankMessage
                return
            }
            if (isSubmitting.value) return
            isSubmitting.value = true
            errorMessage.value = null
            service.saveAnimeSeason(season, onSuccess = { saved ->
                fun proceedFile() {
                    service.handleAnimeSeasonFile(
                        AnimeSeasonFile(
                            id = saved.id,
                            path = filePath.value,
                            isDeleteSource = isDeleteSource.value,
                            isDeleteTarget = isDeleteTarget.value,
                            episodeOffset = episodeOffset.value.toIntOrNull() ?: 0,
                        ),
                        onSuccess = {
                            isSubmitting.value = false
                            onSuccess(saved)
                        },
                        onError = { err ->
                            isSubmitting.value = false
                            errorMessage.value = err
                        }
                    )
                }

                if (saved.anime?.tmdbId != null || anime.tmdbId != null) {
                    service.syncEpisodes(
                        seasonId = saved.id,
                        onSuccess = { proceedFile() },
                        onError = { proceedFile() }
                    )
                } else {
                    proceedFile()
                }
            }, onError = { err ->
                isSubmitting.value = false
                errorMessage.value = err
            })
        }
    }
}
