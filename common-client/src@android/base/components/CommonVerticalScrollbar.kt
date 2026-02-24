package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CommonVerticalScrollbar(modifier: Modifier, lazyListState: LazyListState) {
    // Android has native scrollbars or relies on scrolling without visual scrollbar component
}
