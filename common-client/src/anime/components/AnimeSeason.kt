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
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
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
    val operator = AnimeSeasonOperator(appState)

    if (yearList.value.isEmpty()) {
        operator.loadYears {
            yearList.value = it
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(AcgnNavigationRoute.ANIME_SEASON.localization)
        AcgnLazyColumn(yearListState) {
            items(items = yearList.value, key = { it }) { year ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text(text = year.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        AnimeMonthType.entries.forEach { monthType ->
                            YearMonth(appState, year, monthType)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearMonth(appState: AppState, year: Int, monthType: AnimeMonthType) {
    val seasonListState: LazyListState = rememberLazyListState()
    val seasonList: MutableState<List<AnimeSeason>> = remember { mutableStateOf(listOf()) }
    val operator = AnimeSeasonOperator(appState)

    if (seasonList.value.isEmpty()) {
        operator.loadSeasons(year, monthType) {
            seasonList.value
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(text = monthType.localization, style = MaterialTheme.typography.titleLarge)
        }
    }
}

private class AnimeSeasonOperator(
    val appState: AppState,
) {
    fun loadYears(onSuccess: (List<Int>) -> Unit) {
        appState.scope.launch {
            appState.api.anime.getAnimeYears()
                .onSuccessData(appState, onSuccess)
        }
    }

    fun loadSeasons(year: Int, monthType: AnimeMonthType, onSuccess: (List<AnimeSeason>) -> Unit) {
        appState.scope.launch {
            appState.api.anime.getAnimeSeasonsByYearAndMonth(year, monthType)
                .onSuccessData(appState, onSuccess = onSuccess)
        }
    }
}