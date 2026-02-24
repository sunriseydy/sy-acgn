package dev.sunriseydy.acgn.client.base.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CommonVerticalScrollbar(modifier: Modifier, lazyListState: LazyListState)
