package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.sunriseydy.acgn.client.anime.enums.RssString
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.navigation.AppState

/**
 * @author SunriseYDY
 * @date 2024-08-11 13:23
 */
@Composable
fun Rss(appState: AppState) {
    val rssList = rememberLazyListState()
    val rssId = remember { mutableStateOf(ULong.MIN_VALUE) }
    val rssState = RssState(rssList, rssId)
    Row {
        RssList(Modifier.fillMaxWidth(0.5f), rssState, appState)
        VerticalDivider()
        RssItemList(Modifier.fillMaxWidth(), rssState, appState)
    }
}

@Composable
fun RssList(modifier: Modifier, rssState: RssState, appState: AppState) {

    Column(modifier = modifier) {
        PageTitle(RssString.RSS_TITLE.localization) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Refresh, CommonString.REFRESH.localization)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Add, CommonString.ADD.localization)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Delete, CommonString.DELETE.localization)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Check, RssString.RSS_READ.localization)
            }
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
    val rssList: LazyListState,
    val rssId: MutableState<ULong>,
)