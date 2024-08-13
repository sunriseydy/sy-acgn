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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.enums.RssString
import dev.sunriseydy.acgn.client.common.enums.CommonString
import dev.sunriseydy.acgn.client.components.AcgnAlertDialog
import dev.sunriseydy.acgn.client.components.FormDialog
import dev.sunriseydy.acgn.client.components.PageTitle
import dev.sunriseydy.acgn.client.onSuccess
import dev.sunriseydy.acgn.client.onSuccessData
import dev.sunriseydy.acgn.client.utils.RequiredFieldLabel
import dev.sunriseydy.acgn.client.utils.SupportingText
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-08-11 13:23
 */
@Composable
fun Rss(appState: AppState) {
    val rssId: MutableState<ULong> = remember { mutableStateOf(ULong.MIN_VALUE) }

    Row {
        RssList(Modifier.fillMaxWidth(0.5f), appState)
        VerticalDivider(thickness = 2.dp)
        RssItemList(Modifier.fillMaxWidth(), appState)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RssList(modifier: Modifier, appState: AppState) {
    val init: MutableState<Boolean> = remember { mutableStateOf(false) }
    val addRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val editRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val deleteRssDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val rssListState: LazyListState = rememberLazyListState()
    val rssList: MutableState<List<Rss>> = remember { mutableStateOf(listOf()) }
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

    getAllRss(appState, rssList, true, init)
    Column(modifier = modifier) {
        PageTitle(RssString.RSS_TITLE.localization) {
            IconButton(onClick = { getAllRss(appState, rssList, false, init) }) {
                Icon(Icons.Default.Refresh, CommonString.REFRESH.localization)
            }
            IconButton(onClick = { addRssDialogVisible.value = true }) {
                Icon(Icons.Default.Add, CommonString.ADD.localization)
            }
            IconButton(onClick = {
                runBlocking {
                    appState.api.rss.markRssItemReadByIdOrRssId()
                    getAllRss(appState, rssList, false, init)
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
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(start = 8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(0.5f).padding(top = 12.dp)) {
                                Text(text = rss.title, style = MaterialTheme.typography.titleSmall)
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
                                        getAllRss(appState, rssList, false, init)
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
                                getAllRss(appState, rssList, false, init)
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
                        getAllRss(appState, rssList, false, init)
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
                                getAllRss(appState, rssList, false, init)
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
fun RssItemList(modifier: Modifier, appState: AppState) {
    Column(modifier = modifier) {
        PageTitle(RssString.RSS_ITEM_TITLE.localization)
    }
}

private fun getAllRss(
    appState: AppState,
    rssList: MutableState<List<Rss>>,
    isCheckInit: Boolean = false,
    init: MutableState<Boolean>,
) {
    if (isCheckInit && init.value) return
    runBlocking {
        appState.api.rss.getAllRss().onSuccessData(appState, onSuccess = { data ->
            rssList.value = data
            if (isCheckInit) init.value = true
        })
    }
}