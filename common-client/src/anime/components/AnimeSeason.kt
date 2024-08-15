package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.components.AcgnLazyColumn
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
    val lazyListState: LazyListState = rememberLazyListState()
    val init = remember { mutableStateOf(false) }
    val loading = remember { mutableStateOf(false) }
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
                Icon(Icons.Default.Refresh, null)
            }
        }
        AcgnLazyColumn(modifier = Modifier.fillMaxSize(), lazyListState = lazyListState) {
            sectionMapState.value.forEach { sectionMap ->
                stickyHeader(key = sectionMap.key) {
                    PageTitle(sectionMap.key)
                }
                items(sectionMap.value) { season ->
                    Card(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                            Text(text = season.name, style = MaterialTheme.typography.titleLarge)
                            Text(text = season.description ?: "", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
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
}