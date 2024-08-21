package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.dto.TorrentAdd
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.enums.RssString
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.AcgnAlertDialog
import dev.sunriseydy.acgn.client.components.AcgnLazyColumn
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.components.showError
import dev.sunriseydy.acgn.client.interfaces.Paging
import dev.sunriseydy.acgn.client.interfaces.getPager
import dev.sunriseydy.acgn.client.onSuccess
import dev.sunriseydy.acgn.client.onSuccessData
import dev.sunriseydy.acgn.client.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.utils.RequiredSupportingText
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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

    val rssItemPager: Paging<RssItem> = getPager<RssItem>(
        page = remember { mutableStateOf(1L) },
        size = 50,
        data = remember { mutableStateOf(listOf()) },
        loading = remember { mutableStateOf(false) },
        finished = remember { mutableStateOf(false) },
        onError = { e ->
            appState.showError(e.message ?: CommonString.API_ERROR.localization)
        },
    ) { pager ->
        appState.api.rss.getRssItemByRssIdOrIsRead(
            rssId = if (currentRss.value.id == ULong.MIN_VALUE) null else currentRss.value.id,
            isRead = if (isOnlyUnread.value == true) false else null,
            page = pager.page.value,
            size = pager.size,
        ).checkSuccessAndNotNull()
    }

    val rssOperator: RssOperator = RssOperator(appState, rssList, rssItemPager)

    // 加载数据
    rssOperator.loadData()

    // 渲染页面
    Row {
        RssList(Modifier.fillMaxWidth(0.5f), rssList, currentRss, rssOperator)
        VerticalDivider(thickness = 2.dp)
        RssItemList(Modifier.fillMaxWidth(), isOnlyUnread, rssItemPager.data, currentRss, rssOperator)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RssList(
    modifier: Modifier,
    rssList: MutableState<List<Rss>>,
    currentRss: MutableState<Rss>,
    rssOperator: RssOperator,
) {
    val addRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val editRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val deleteRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val rssListState: LazyListState = rememberLazyListState()
    val newLink = remember { mutableStateOf("") }
    val newTitle = remember { mutableStateOf("") }
    val deleteRss: MutableState<Rss?> = remember { mutableStateOf(null) }
    val editRss: MutableState<Rss?> = remember { mutableStateOf(null) }
    val isError = rememberSaveable { mutableStateOf(false) }

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
        PageTitle(RssString.RSS_TITLE.localization) {
            IconButton(onClick = { rssOperator.loadRss() }) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = { addRssDialogVisible.value = true }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = { rssOperator.markRssItemReadByIdOrRssId(null, null) }) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(48.dp))
            }
        }
        AcgnLazyColumn(modifier = Modifier.fillMaxSize(), lazyListState = rssListState) {
            items(rssList.value, key = { it.id }) { rss ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        currentRss.value = rss
                        rssOperator.loadRssItem()
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
                                Icon(Icons.Default.Delete, CommonString.DELETE.localization)
                            }
                            IconButton(onClick = {
                                editRss.value = rss
                                newTitle.value = rss.title
                                editRssDialogVisible.value = true
                            }) {
                                Icon(Icons.Default.Edit, CommonString.UPDATE.localization)
                            }
                            IconButton(onClick = { rssOperator.markRssItemReadByIdOrRssId(null, rss.id) }) {
                                Icon(Icons.Default.Check, RssString.RSS_READ.localization)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    // 创建 RSS 弹窗
    FormDialog(
        formDialogVisible = addRssDialogVisible,
        onConfirmation = {
            require(newLink.value.isNotBlank()) { RssString.RSS_FIELD_LINK.localization + CommonString.IS_BLANK.localization }
            rssOperator.createRss(
                newLink.value,
                onSuccess = {
                    closeAddRssDialog()
                    rssOperator.loadRss()
                },
                onError = {
                    throw error(it)
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
            label = { RequiredFieldLabel(RssString.RSS_FIELD_LINK.localization) },
            supportingText = { RequiredSupportingText(newLink, RssString.RSS_FIELD_LINK.localization) }
        )
    }
    // 删除 RSS 弹窗
    AcgnAlertDialog(
        alertDialogVisible = deleteRssDialogVisible,
        onConfirmation = {
            deleteRss.value?.also {
                rssOperator.deleteRss(it.id) {
                    deleteRssDialogVisible.value = false
                    rssOperator.loadRss()
                }
            }
        },
        dialogTitle = CommonString.DELETE.localization + deleteRss.value?.title,
    )
    // 更新 RSS 弹窗
    FormDialog(
        formDialogVisible = editRssDialogVisible,
        onConfirmation = {
            require(newTitle.value.isNotBlank()) { RssString.RSS_FIELD_TITLE.localization + CommonString.IS_BLANK.localization }
            editRss.value?.also {
                rssOperator.updateRss(
                    it.copy(id = it.id, title = newTitle.value),
                    onSuccess = {
                        closeEditRssDialog()
                        rssOperator.loadRss()
                    },
                    onError = {
                        throw error(it)
                    }
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
            label = { RequiredFieldLabel(RssString.RSS_FIELD_TITLE.localization) },
            supportingText = { RequiredSupportingText(newTitle, RssString.RSS_FIELD_TITLE.localization) },
        )
    }
}

@Composable
fun RssItemList(
    modifier: Modifier,
    isOnlyUnread: MutableState<Boolean>,
    rssItemList: MutableState<List<RssItem>>,
    currentRss: MutableState<Rss>,
    rssOperator: RssOperator,
) {
    val rssItemListState: LazyListState = rememberLazyListState()
    val downloadDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val downloadRssItem: MutableState<RssItem?> = remember { mutableStateOf(null) }

    Column(modifier = modifier) {
        PageTitle(RssString.RSS_ITEM_TITLE.localization) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(RssString.IS_ONLY_UNREAD.localization)
                Checkbox(
                    checked = isOnlyUnread.value,
                    onCheckedChange = {
                        isOnlyUnread.value = it
                        rssOperator.loadRssItem()
                    }
                )
            }
        }
        AcgnLazyColumn(modifier = Modifier.fillMaxSize(), lazyListState = rssItemListState) {
            itemsIndexed(rssItemList.value, key = { index, rssItem -> rssItem.id }) { index, rssItem ->
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
                            IconButton(onClick = { rssOperator.markRssItemReadByIdOrRssId(rssItem.id, null) }) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    }
                    HorizontalDivider(thickness = 4.dp)
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            rssItem.publishedAt.toLocalDateTime(
                                TimeZone.currentSystemDefault()
                            ).toString()
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
                                rssOperator.loadMoreRssItem()
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
                rssOperator.download(it.link) {
                    rssOperator.markRssItemReadByIdOrRssId(id = it.id)
                }
                downloadRssItem.value = null
                downloadDialogVisible.value = false
            }
        },
        dialogTitle = CommonString.DOWNLOAD.localization + downloadRssItem.value?.title,
    )
}

class RssOperator(
    val appState: AppState,
    val rssList: MutableState<List<Rss>>,
    val rssItemPager: Paging<RssItem>,
) {
    fun loadRss() {
        appState.scope.launch {
            appState.api.rss.getAllRss().onSuccessData(appState, onSuccess = { data ->
                rssList.value = data
            })
        }
    }

    fun createRss(
        link: String,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        appState.scope.launch {
            appState.api.rss.createRss(link)
                .onSuccess(null, onSuccess = onSuccess, onError = onError)
        }
    }

    fun deleteRss(id: ULong, onSuccess: () -> Unit) {
        appState.scope.launch {
            appState.api.rss.deleteRss(id).onSuccess(appState, onSuccess)
        }
    }

    fun updateRss(
        rss: Rss,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        appState.scope.launch {
            appState.api.rss.saveRss(rss.id, rss)
                .onSuccess(appState, onSuccess, onError)
        }
    }

    fun markRssItemReadByIdOrRssId(
        id: String? = null,
        rssId: ULong? = null
    ) {
        appState.scope.launch {
            appState.api.rss.markRssItemReadByIdOrRssId(id, rssId).onSuccess(appState, onSuccess = { loadData() })
        }
    }

    fun loadRssItem() {
        appState.scope.launch {
            rssItemPager.loadInit()
        }
    }

    fun loadMoreRssItem() {
        appState.scope.launch {
            rssItemPager.loadNext()
        }
    }

    fun loadData() {
        if (rssList.value.isEmpty()) {
            loadRss()
        }
        loadRssItem()
    }

    fun download(link: String, onSuccess: () -> Unit) {
        appState.scope.launch {
            appState.api.rss.addQbTorrent(TorrentAdd(url = link)).onSuccess(appState, onSuccess)
        }
    }
}