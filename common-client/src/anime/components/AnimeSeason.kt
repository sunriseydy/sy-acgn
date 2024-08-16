package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.enums.AnimeString
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.navigation.AcgnNavigationRoute
import dev.sunriseydy.acgn.client.onSuccessData
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
    CreateAnimeSeason(appState, createDialogVisible)
}

@Composable
private fun CreateAnimeSeason(
    appState: AppState,
    createDialogVisible: MutableState<Boolean>,
    onSuccess: () -> Unit = { },
) {
    val isCreateAnime = remember { mutableStateOf(false) }
    val animeName: MutableState<String?> = remember { mutableStateOf(null) }
    val animeSeasonNumber: MutableState<Int?> = remember { mutableStateOf(null) }
    val animeSeasonName: MutableState<String?> = remember { mutableStateOf(null) }

    fun closeCreateDialog() {
        createDialogVisible.value = false
        isCreateAnime.value = false
        animeName.value = null
        animeSeasonNumber.value = null
        animeSeasonName.value = null
    }

    FormDialog(
        formDialogVisible = createDialogVisible,
        onDismissRequest = { closeCreateDialog() },
        onConfirmation = {
            onSuccess()
        },
    ) {
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
}