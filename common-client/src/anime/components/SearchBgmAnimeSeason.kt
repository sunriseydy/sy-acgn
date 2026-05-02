package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.res.Res
import dev.sunriseydy.acgn.client.res.no_data
import dev.sunriseydy.acgn.client.res.search
import dev.sunriseydy.acgn.client.res.search_bangumi
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.stringResource

/**
 * 搜索 Bangumi 动画季度弹窗
 *
 * @author SunriseYDY
 * @date 2026-02-15
 */
@Composable
fun SearchBgmAnimeSeason(
    animeSeasonService: AnimeSeasonService,
    visible: MutableState<Boolean>,
    currentSeason: AnimeSeason?,
    onSuccess: () -> Unit = { },
) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateOf(emptyList<AnimeSeason>()) }
    val searchLoading = remember { mutableStateOf(false) }
    val searchExpanded = remember { mutableStateOf(false) }
    val selected: MutableState<AnimeSeason?> = remember { mutableStateOf(null) }

    // 初始化搜索关键词
    LaunchedEffect(visible.value) {
        if (visible.value && currentSeason != null) {
            searchQuery.value = currentSeason.anime!!.name
            selected.value = currentSeason
        }
    }

    fun closeDialog() {
        visible.value = false
        searchQuery.value = ""
        searchResults.value = emptyList()
        searchLoading.value = false
        searchExpanded.value = false
    }

    fun handleSearch() {
        if (searchQuery.value.isNotBlank()) {
            searchLoading.value = true
            selected.value = null
            animeSeasonService.searchBgmAnime(
                query = searchQuery.value,
                onSuccess = {
                    searchResults.value = it
                    searchLoading.value = false
                    searchExpanded.value = true
                },
                onError = {
                    searchLoading.value = false
                }
            )
        }
    }

    fun handleConfirm() {
        if (currentSeason != null && selected.value != null && selected.value!!.additions.isNotEmpty()) {
            val additionalInfo = selected.value!!.additions[0]
            // 判断是否存在
            val existingAddition = AnimeAdditionType.BgmJson.additionalInfo(currentSeason.additions)
            val newAdditions = currentSeason.additions.filter {
                it.additionalType != AnimeAdditionType.BgmJson.key
            } + (existingAddition?.copy(additionalValue = additionalInfo.additionalValue)
                ?: additionalInfo.copy(associatedId = currentSeason.id))

            val newSeason = currentSeason.copy(
                bgmId = selected.value!!.bgmId,
                additions = newAdditions
            )
            animeSeasonService.saveAnimeSeason(newSeason, onSuccess = {
                onSuccess()
                closeDialog()
            })
        }
    }

    FormDialog(
        formDialogVisible = visible,
        onDismissRequest = { closeDialog() },
        onConfirmation = { handleConfirm() },
    ) {
        val fieldWidth = 500.dp

        Column {
            // Search Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    modifier = Modifier.width(fieldWidth),
                    label = { Text("${stringResource(Res.string.search)} ${stringResource(Res.string.search_bangumi)}") },
                    trailingIcon = {
                        IconButton(onClick = { handleSearch() }) {
                            if (searchLoading.value) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Search, null)
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Dropdown Results
            Box {
                DropdownMenu(
                    expanded = searchExpanded.value,
                    onDismissRequest = { searchExpanded.value = false },
                    scrollState = rememberScrollState(),
                    modifier = Modifier.width(fieldWidth).heightIn(max = 300.dp)
                ) {
                    if (searchResults.value.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.no_data)) },
                            onClick = { searchExpanded.value = false },
                            enabled = false
                        )
                    } else {
                        searchResults.value.forEach { result ->
                            DropdownMenuItem(
                                text = { Text("${result.name} (${result.year})") },
                                onClick = {
                                    searchExpanded.value = false
                                    selected.value = result
                                }
                            )
                        }
                    }
                }
            }

            // 展示搜索结果的别名
            if (selected.value != null && selected.value!!.bgmJson != null) {
                val bgmJson = selected.value!!.bgmJson!!
                bgmJson.get("infobox")?.jsonArray?.forEach { infobox ->
                    if (infobox.jsonObject.get("key")?.jsonPrimitive?.content == "别名") {
                        infobox.jsonObject.get("value")?.jsonArray?.forEach { alias ->
                            alias.jsonObject.get("v")?.jsonPrimitive?.content?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.width(fieldWidth)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}