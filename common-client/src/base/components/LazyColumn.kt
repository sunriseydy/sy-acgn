package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @author SunriseYDY
 * @date 2024-08-15 16:42
 */
@Composable
fun LazyColumn(modifier: Modifier = Modifier, lazyListState: LazyListState, content: LazyListScope.() -> Unit) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            content = content,
        )
        CommonVerticalScrollbar(
            modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterEnd).fillMaxHeight(),
            lazyListState = lazyListState
        )
    }
}