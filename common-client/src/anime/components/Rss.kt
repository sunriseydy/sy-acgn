package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.enums.RssString
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.onSuccess
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-08-11 13:23
 */
@Composable
fun Rss(appState: AppState) {
    val rssState = RssState(
        rssListState = rememberLazyListState(),
        rssId = remember { mutableStateOf(ULong.MIN_VALUE) },
        rssList = remember { mutableStateOf(listOf()) }
    )
    Row {
        RssList(Modifier.fillMaxWidth(0.5f), rssState, appState)
        VerticalDivider(thickness = 2.dp)
        RssItemList(Modifier.fillMaxWidth(), rssState, appState)
    }
}

@Composable
fun RssList(modifier: Modifier, rssState: RssState, appState: AppState) {
    // 首次加载
    if (rssState.rssList.value.isEmpty()) {
        getAllRss(appState, rssState)
    }
    Column(modifier = modifier) {
        PageTitle(RssString.RSS_TITLE.localization) {
            IconButton(onClick = {
                getAllRss(appState, rssState)
            }) {
                Icon(Icons.Default.Refresh, CommonString.REFRESH.localization)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Add, CommonString.ADD.localization)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Check, RssString.RSS_READ.localization)
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rssState.rssListState,
                contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            ) {
                items(rssState.rssList.value, key = { it.id }) { rss ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.5f).padding(top = 12.dp),
                            ) {
                                Text(text = rss.title, style = MaterialTheme.typography.titleSmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Delete, CommonString.DELETE.localization)
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Check, RssString.RSS_READ.localization)
                                }
                            }
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = rssState.rssListState
                )
            )
        }
    }
}

@Composable
fun RssItemList(modifier: Modifier, rssState: RssState, appState: AppState) {
    Column(modifier = modifier) {
        PageTitle(RssString.RSS_ITEM_TITLE.localization)
    }
}

data class RssState(
    val rssListState: LazyListState,
    val rssId: MutableState<ULong>,
    val rssList: MutableState<List<Rss>>,
)

fun getAllRss(appState: AppState, rssState: RssState) {
    runBlocking {
        appState.api.rss.getAllRss().onSuccess(appState) { data ->
            rssState.rssList.component2()(data)
        }
    }
}