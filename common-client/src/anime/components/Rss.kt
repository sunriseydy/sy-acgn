package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
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
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.enums.RssString
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.AcgnAlertDialog
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.components.showError
import dev.sunriseydy.acgn.client.interfaces.getPager
import dev.sunriseydy.acgn.client.onSuccess
import dev.sunriseydy.acgn.client.onSuccessData
import dev.sunriseydy.acgn.client.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.utils.SupportingText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-08-11 13:23
 */
@Composable
fun Rss(appState: AppState) {
    val rssId: MutableState<ULong> = remember { mutableStateOf(ULong.MIN_VALUE) }
    val rssList: MutableState<List<Rss>> = remember { mutableStateOf(listOf()) }
    val isOnlyUnread = remember { mutableStateOf(true) }

    fun loadRss() {
        appState.scope.launch {
            appState.api.rss.getAllRss().onSuccessData(appState, onSuccess = { data ->
                rssList.value = data
            })
        }
    }

    val rssItemPager = getPager<RssItem>(
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
            rssId = if (rssId.value == ULong.MIN_VALUE) null else rssId.value,
            isRead = if (isOnlyUnread.value == true) false else null,
            page = pager.page.value,
            size = pager.size,
        ).checkSuccessAndNotNull()
    }
    // 加载数据
    if (rssList.value.isEmpty()) {
        loadRss()
    }
    appState.scope.launch {
        rssItemPager.loadInit()
    }
    // 渲染页面
    Row {
        RssList(Modifier.fillMaxWidth(0.5f), appState, rssList,
            loadRss = { loadRss() },
            loadRssItem = { it ->
                rssId.value = it
                appState.scope.launch {
                    rssItemPager.loadInit()
                }
            })
        VerticalDivider(thickness = 2.dp)
        RssItemList(Modifier.fillMaxWidth(), appState, isOnlyUnread, rssItemPager.data)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RssList(
    modifier: Modifier,
    appState: AppState,
    rssList: MutableState<List<Rss>>,
    loadRss: () -> Unit,
    loadRssItem: (ULong) -> Unit,
) {
    val init: MutableState<Boolean> = remember { mutableStateOf(false) }
    val addRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val editRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val deleteRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val rssListState: LazyListState = rememberLazyListState()
    val newLink = remember { mutableStateOf("") }
    val newTitle = remember { mutableStateOf("") }
    val deleteRss: MutableState<Rss?> = remember { mutableStateOf(null) }
    val editRss: MutableState<Rss?> = remember { mutableStateOf(null) }
    val isError = rememberSaveable { mutableStateOf(false) }
    val errorMessage = rememberSaveable { mutableStateOf("") }

    fun setError(message: String) {
        isError.value = true
        errorMessage.value = message
    }

    fun clearError() {
        isError.value = false
        errorMessage.value = ""
    }

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
            IconButton(onClick = { loadRss() }) {
                Icon(Icons.Default.Refresh, CommonString.REFRESH.localization)
            }
            IconButton(onClick = { addRssDialogVisible.value = true }) {
                Icon(Icons.Default.Add, CommonString.ADD.localization)
            }
            IconButton(onClick = {
                runBlocking {
                    appState.api.rss.markRssItemReadByIdOrRssId()
                    loadRss()
                }
            }) {
                Icon(Icons.Default.Check, RssString.RSS_READ.localization)
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rssListState,
                contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            ) {
                items(rssList.value, key = { it.id }) { rss ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            loadRssItem(rss.id)
                        }
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(0.5f).align(Alignment.CenterVertically)) {
                                Text(text = rss.title, style = MaterialTheme.typography.titleLarge)
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
                                IconButton(onClick = {
                                    runBlocking {
                                        appState.api.rss.markRssItemReadByIdOrRssId(rssId = rss.id)
                                        loadRss()
                                    }
                                }) {
                                    Icon(Icons.Default.Check, RssString.RSS_READ.localization)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            VerticalScrollbar(
                modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = rssListState
                )
            )
        }
        // 创建 RSS 弹窗
        FormDialog(
            formDialogVisible = addRssDialogVisible,
            onConfirmation = {
                if (newLink.value.isBlank()) {
                    setError(RssString.RSS_FIELD_LINK.localization + CommonString.IS_BLANK.localization)
                } else {
                    runBlocking {
                        appState.api.rss.createRss(newLink.value).onSuccess(
                            onSuccess = {
                                clearError()
                                closeAddRssDialog()
                                loadRss()
                            },
                            onError = {
                                setError(it)
                            },
                        )
                    }
                }
            },
            onDismissRequest = {
                closeAddRssDialog()
                clearError()
            },
        ) {
            OutlinedTextField(
                value = newLink.value,
                onValueChange = { newLink.value = it },
                label = { RequiredFieldLabel(RssString.RSS_FIELD_LINK.localization) },
                isError = isError.value,
                supportingText = { SupportingText(isError.value, errorMessage.value) }
            )
        }
        // 删除 RSS 弹窗
        AcgnAlertDialog(
            alertDialogVisible = deleteRssDialogVisible,
            onConfirmation = {
                deleteRss.value?.also {
                    runBlocking {
                        appState.api.rss.deleteRss(it.id)
                        deleteRssDialogVisible.value = false
                        loadRss()
                    }
                }
            },
            dialogTitle = CommonString.DELETE.localization + deleteRss.value?.title,
        )
        // 更新 RSS 弹窗
        FormDialog(
            formDialogVisible = editRssDialogVisible,
            onConfirmation = {
                if (newTitle.value.isBlank()) {
                    setError(RssString.RSS_FIELD_TITLE.localization + CommonString.IS_BLANK.localization)
                } else {
                    editRss.value?.also {
                        runBlocking {
                            appState.api.rss.saveRss(it.id, it.copy(title = newTitle.value)).onSuccess(onSuccess = {
                                clearError()
                                closeEditRssDialog()
                                loadRss()
                            }, onError = {
                                setError(it)
                            })
                        }
                    }
                }
            },
            onDismissRequest = {
                clearError()
                closeEditRssDialog()
            },
        ) {
            OutlinedTextField(
                value = newTitle.value,
                onValueChange = { newTitle.value = it },
                label = { RequiredFieldLabel(RssString.RSS_FIELD_TITLE.localization) },
                isError = isError.value,
                supportingText = { SupportingText(isError.value, errorMessage.value) },
            )
        }
    }
}

@Composable
fun RssItemList(
    modifier: Modifier,
    appState: AppState,
    isOnlyUnread: MutableState<Boolean>,
    rssItemList: MutableState<List<RssItem>>,
) {
    val rssItemListState: LazyListState = rememberLazyListState()

    Column(modifier = modifier) {
        PageTitle(RssString.RSS_ITEM_TITLE.localization) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(RssString.IS_ONLY_UNREAD.localization)
                Checkbox(
                    checked = isOnlyUnread.value,
                    onCheckedChange = { isOnlyUnread.value = it }
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rssItemListState,
                contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            ) {
                items(rssItemList.value, key = { it.id }) { rssItem ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.CenterVertically)) {
                                Text(text = rssItem.title, style = MaterialTheme.typography.titleLarge)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    onClick = { }
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                }
                                IconButton(
                                    onClick = { }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        }
                        HorizontalDivider(thickness = 4.dp)
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text(text = rssItem.description ?: "")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            VerticalScrollbar(
                modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = rssItemListState
                )
            )
        }
    }
}