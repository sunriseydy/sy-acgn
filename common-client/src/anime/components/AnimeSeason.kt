package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
fun AnimeSeason(appState: AppState) {
    val yearListState: LazyListState = rememberLazyListState()
    val yearList: MutableState<List<Int>> = remember { mutableStateOf(listOf()) }
    val operator = AnimeSeasonOperator(appState, yearList)

    operator.loadYears()

    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(AcgnNavigationRoute.ANIME_SEASON.localization)
        AcgnLazyColumn(yearListState) {
            items(items = yearList.value, key = { it }) { year ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(text = year.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

class AnimeSeasonOperator(val appState: AppState, val yearList: MutableState<List<Int>>) {
    fun loadYears() {
        if (yearList.value.isNotEmpty()) return
        appState.scope.launch {
            appState.api.anime.getAnimeYears()
                .onSuccessData(appState, onSuccess = {
                    yearList.value = it
                })
        }
    }
}