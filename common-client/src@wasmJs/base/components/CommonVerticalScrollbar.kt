package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CommonVerticalScrollbar(modifier: Modifier, lazyListState: LazyListState) {
    // WasmJs does not support desktop-style scrollbar, browser handles scrolling natively
}
