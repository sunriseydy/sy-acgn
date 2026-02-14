package dev.sunriseydy.acgn.client.anime.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.service.RssService
import dev.sunriseydy.acgn.client.base.components.*
import dev.sunriseydy.acgn.client.base.enums.AcgnContentType
import dev.sunriseydy.acgn.client.base.interfaces.Paging
import dev.sunriseydy.acgn.client.base.interfaces.getPager
import dev.sunriseydy.acgn.client.base.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.base.utils.RequiredSupportingText
import dev.sunriseydy.acgn.client.res.*
import org.jetbrains.compose.resources.stringResource

/**
 * @author SunriseYDY
 * @date 2024-08-11 13:23
 */
@Composable
fun Rss(appState: AppState) {
    val currentRss: MutableState<Rss> = remember {
        mutableStateOf(
            Rss(
                id = ULong.MIN_VALUE,
                link = "",
                title = "",
            )
        )
    }
    val rssList: MutableState<List<Rss>> = remember { mutableStateOf(listOf()) }
    val isOnlyUnread: MutableState<Boolean> = remember { mutableStateOf(true) }
    val init: MutableState<Boolean> = remember { mutableStateOf(false) }

    val apiError = stringResource(Res.string.api_error)
    val rssItemPager: Paging<RssItem> = getPager(
        page = remember { mutableStateOf(1L) },
        size = 50,
        data = remember { mutableStateOf(listOf()) },
        loading = remember { mutableStateOf(false) },
        finished = remember { mutableStateOf(false) },
        onError = { e ->
            appState.showError(e.message ?: apiError)
        },
    ) { pager ->
        appState.api.rss.getQbRssArticles(
            rssId = if (currentRss.value.id == ULong.MIN_VALUE) null else currentRss.value.id,
            isRead = if (isOnlyUnread.value) false else null,
            page = pager.page.value,
            size = pager.size,
        ).checkSuccessAndNotNull()
    }

    val rssService = RssService(appState, rssList, rssItemPager)

    // 加载数据
    if (!init.value) {
        init.value = true
        rssService.loadData()
    }

    // 渲染页面
    if (appState.contentType == AcgnContentType.DUAL_PANE) {
        Row {
            RssList(Modifier.fillMaxWidth(0.5f), rssList, currentRss, rssService)
            VerticalDivider(thickness = 2.dp)
            RssItemList(Modifier.fillMaxWidth(), isOnlyUnread, rssItemPager.data, currentRss, rssService)
        }
    } else {
        if (currentRss.value.id == ULong.MIN_VALUE) {
            // 渲染订阅列表
            RssList(Modifier.fillMaxWidth(), rssList, currentRss, rssService)
        } else {
            // 渲染订阅内容
            RssItemList(Modifier.fillMaxWidth(), isOnlyUnread, rssItemPager.data, currentRss, rssService)
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RssList(
    modifier: Modifier,
    rssList: MutableState<List<Rss>>,
    currentRss: MutableState<Rss>,
    rssService: RssService,
) {
    val addRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val editRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val deleteRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val rssListState: LazyListState = rememberLazyListState()
    val newLink = remember { mutableStateOf("") }
    val newTitle = remember { mutableStateOf("") }
    val deleteRss: MutableState<Rss?> = remember { mutableStateOf(null) }
    val editRss: MutableState<Rss?> = remember { mutableStateOf(null) }

    fun closeAddRssDialog() {
        newLink.value = ""
        addRssDialogVisible.value = false
    }

    fun closeEditRssDialog() {
        newTitle.value = ""
        editRss.value = null
        editRssDialogVisible.value = false
    }

    Column(modifier = modifier) {
        PageTitle(stringResource(Res.string.rss_title)) {
            IconButton(onClick = {
                // 刷新所有 RSS 订阅
                rssService.refreshRss()
            }) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = { addRssDialogVisible.value = true }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = { rssService.markRssItemReadByIdOrRssId(null, null) }) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(48.dp))
            }
        }
        AcgnLazyColumn(modifier = Modifier.fillMaxSize(), lazyListState = rssListState) {
            items(rssList.value, key = { it.id }) { rss ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        currentRss.value = rss
                        rssService.loadRssItem()
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (rss.id == currentRss.value.id)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(0.5f).align(Alignment.CenterVertically)) {
                            Text(text = rss.title, style = MaterialTheme.typography.titleLarge)
                            Text(text = "(${rss.unreadCount})")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            IconButton(onClick = {
                                deleteRss.value = rss
                                deleteRssDialogVisible.value = true
                            }) {
                                Icon(Icons.Default.Delete, stringResource(Res.string.delete))
                            }
                            IconButton(onClick = {
                                editRss.value = rss
                                newTitle.value = rss.title
                                editRssDialogVisible.value = true
                            }) {
                                Icon(Icons.Default.Edit, stringResource(Res.string.update))
                            }
                            IconButton(onClick = { rssService.markRssItemReadByIdOrRssId(null, rss.id) }) {
                                Icon(Icons.Default.Check, stringResource(Res.string.rss_read))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    // 创建 RSS 弹窗
    val linkRequiredMsg = stringResource(Res.string.rss_field_link) + stringResource(Res.string.is_blank)
    FormDialog(
        formDialogVisible = addRssDialogVisible,
        onConfirmation = {
            require(newLink.value.isNotBlank()) { linkRequiredMsg }
            rssService.createRss(
                newLink.value,
                onSuccess = {
                    closeAddRssDialog()
                    rssService.loadRss()
                },
            )
        },
        onDismissRequest = {
            closeAddRssDialog()
        },
    ) {
        OutlinedTextField(
            value = newLink.value,
            onValueChange = { newLink.value = it },
            label = { RequiredFieldLabel(stringResource(Res.string.rss_field_link)) },
            supportingText = { RequiredSupportingText(newLink, stringResource(Res.string.rss_field_link)) }
        )
    }
    // 删除 RSS 弹窗
    AcgnAlertDialog(
        alertDialogVisible = deleteRssDialogVisible,
        onConfirmation = {
            deleteRss.value?.also {
                rssService.deleteRss(it.id) {
                    deleteRssDialogVisible.value = false
                    rssService.loadRss()
                }
            }
        },
        dialogTitle = stringResource(Res.string.delete) + deleteRss.value?.title,
    )
    // 更新 RSS 弹窗
    val titleRequiredMsg = stringResource(Res.string.rss_field_title) + stringResource(Res.string.is_blank)
    FormDialog(
        formDialogVisible = editRssDialogVisible,
        onConfirmation = {
            require(newTitle.value.isNotBlank()) { titleRequiredMsg }
            editRss.value?.also {
                rssService.updateRss(
                    it.copy(id = it.id, title = newTitle.value),
                    onSuccess = {
                        closeEditRssDialog()
                        rssService.loadRss()
                    },
                )
            }
        },
        onDismissRequest = {
            closeEditRssDialog()
        },
    ) {
        OutlinedTextField(
            value = newTitle.value,
            onValueChange = { newTitle.value = it },
            label = { RequiredFieldLabel(stringResource(Res.string.rss_field_title)) },
            supportingText = { RequiredSupportingText(newTitle, stringResource(Res.string.rss_field_title)) },
        )
    }
}

@Composable
fun RssItemList(
    modifier: Modifier,
    isOnlyUnread: MutableState<Boolean>,
    rssItemList: MutableState<List<RssItem>>,
    currentRss: MutableState<Rss>,
    rssService: RssService,
) {
    val rssItemListState: LazyListState = rememberLazyListState()
    val downloadDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val downloadRssItem: MutableState<RssItem?> = remember { mutableStateOf(null) }

    Column(modifier = modifier) {
        PageTitle(stringResource(Res.string.rss_item_title)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(Res.string.is_only_unread))
                Checkbox(
                    checked = isOnlyUnread.value,
                    onCheckedChange = {
                        isOnlyUnread.value = it
                        rssService.loadRssItem()
                    }
                )
                if (currentRss.value.id != ULong.MIN_VALUE) {
                    IconButton(
                        onClick = {
                            currentRss.value = Rss(id = ULong.MIN_VALUE, title = "", link = "")
                            rssService.loadData()
                        }
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
        AcgnLazyColumn(modifier = Modifier.fillMaxSize(), lazyListState = rssItemListState) {
            itemsIndexed(rssItemList.value, key = { _, rssItem -> rssItem.id }) { index, rssItem ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.CenterVertically)) {
                            SelectionContainer {
                                Text(text = rssItem.title, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            IconButton(
                                onClick = {
                                    downloadRssItem.value = rssItem
                                    downloadDialogVisible.value = true
                                }
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                            }
                            IconButton(onClick = { rssService.markRssItemReadByIdOrRssId(rssItem.id, rssItem.rssId) }) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    }
                    HorizontalDivider(thickness = 4.dp)
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            rssItem.publishedAt.toLocalDateTime().toString()
                        )
                        if (currentRss.value.id == ULong.MIN_VALUE) {
                            rssItem.rss?.let { Text(it.title) }
                        }
                        rssItem.description?.let { Text(it) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (index == rssItemList.value.lastIndex) {
                        IconButton(
                            onClick = {
                                rssService.loadMoreRssItem()
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(48.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    // 下载弹窗
    AcgnAlertDialog(
        alertDialogVisible = downloadDialogVisible,
        onConfirmation = {
            downloadRssItem.value?.also {
                rssService.download(it.link) {
                    rssService.markRssItemReadByIdOrRssId(id = it.id, rssId = it.rssId)
                }
                downloadRssItem.value = null
                downloadDialogVisible.value = false
            }
        },
        dialogTitle = stringResource(Res.string.download) + downloadRssItem.value?.title,
    )
}