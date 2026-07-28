package dev.sunriseydy.acgn.client.novel.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.client.base.components.AlertDialog
import dev.sunriseydy.acgn.client.base.components.AttachImage
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.base.components.PageTitle
import dev.sunriseydy.acgn.client.base.navigation.NovelDetailRoute
import dev.sunriseydy.acgn.client.base.navigation.TopLevelRouteEnum
import dev.sunriseydy.acgn.client.res.*
import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelCreateOrUpdateDto
import dev.sunriseydy.acgn.novel.enums.NovelStatusEnum
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun NovelListPage(appState: AppState) {
    val novelList = remember { mutableStateOf<List<Novel>>(emptyList()) }
    val loading = remember { mutableStateOf(false) }
    val searchName = remember { mutableStateOf("") }
    val selectedStatus = remember { mutableStateOf<String?>(null) }

    // Dialog states
    val createDialogVisible = remember { mutableStateOf(false) }
    val bgmImportDialogVisible = remember { mutableStateOf(false) }
    val deleteDialogVisible = remember { mutableStateOf(false) }
    val selectedNovel = remember { mutableStateOf<Novel?>(null) }

    // Create / Edit form states
    val formName = remember { mutableStateOf("") }
    val formOriginalName = remember { mutableStateOf("") }
    val formAuthor = remember { mutableStateOf("") }
    val formIllustrator = remember { mutableStateOf("") }
    val formPublisher = remember { mutableStateOf("") }
    val formDescription = remember { mutableStateOf("") }
    val formStatus = remember { mutableStateOf(NovelStatusEnum.SERIALIZING.name) }

    // BGM Import state
    val bgmIdInput = remember { mutableStateOf("") }
    val bgmSearchQuery = remember { mutableStateOf("") }
    val bgmSearchResults = remember { mutableStateOf<List<Novel>>(emptyList()) }
    val bgmSearching = remember { mutableStateOf(false) }

    fun loadNovels() {
        if (loading.value) return
        loading.value = true
        appState.scope.launch {
            appState.api.novel.getNovelList(
                name = searchName.value.ifBlank { null },
                status = selectedStatus.value,
            ).onSuccessData(appState, onSuccess = { list ->
                novelList.value = list
                loading.value = false
            })
        }
    }

    LaunchedEffect(Unit) {
        loadNovels()
    }

    fun openCreateDialog(novel: Novel? = null) {
        selectedNovel.value = novel
        formName.value = novel?.name.orEmpty()
        formOriginalName.value = novel?.originalName.orEmpty()
        formAuthor.value = novel?.author.orEmpty()
        formIllustrator.value = novel?.illustrator.orEmpty()
        formPublisher.value = novel?.publisher.orEmpty()
        formDescription.value = novel?.description.orEmpty()
        formStatus.value = novel?.status ?: NovelStatusEnum.SERIALIZING.name
        createDialogVisible.value = true
    }

    fun openBgmImportDialog() {
        bgmIdInput.value = ""
        bgmSearchQuery.value = ""
        bgmSearchResults.value = emptyList()
        bgmSearching.value = false
        bgmImportDialogVisible.value = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(TopLevelRouteEnum.NOVEL.meaning) {
            OutlinedTextField(
                value = searchName.value,
                onValueChange = { searchName.value = it },
                label = { Text(stringResource(Res.string.novel_search_placeholder)) },
                singleLine = true,
                modifier = Modifier.padding(end = 8.dp).width(200.dp)
            )
            IconButton(onClick = { loadNovels() }) {
                Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.search))
            }
            IconButton(onClick = {
                searchName.value = ""
                selectedStatus.value = null
                loadNovels()
            }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh))
            }
            IconButton(onClick = { openCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.novel_create))
            }
            IconButton(onClick = { openBgmImportDialog() }) {
                Icon(Icons.Default.CloudDownload, contentDescription = stringResource(Res.string.novel_bangumi_import))
            }
        }

        if (loading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (novelList.value.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.novel_no_data))
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 320.dp),
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyStaggeredGridState(),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(novelList.value) { novel ->
                    Card(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        onClick = {
                            appState.navigationAction.add(NovelDetailRoute(novel.id))
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val posterId = novel.posterId
                            if (!posterId.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    AttachImage(
                                        appState = appState,
                                        attachId = posterId,
                                        modifier = Modifier.width(300.dp).height(400.dp),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                            SelectionContainer {
                                Text(
                                    text = novel.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            val origName = novel.originalName
                            if (!origName.isNullOrBlank()) {
                                Text(
                                    text = origName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val authorLabel = stringResource(Res.string.novel_author)
                            val illustratorLabel = stringResource(Res.string.novel_illustrator)
                            Text(
                                text = "$authorLabel: ${novel.author ?: "-"}  |  $illustratorLabel: ${novel.illustrator ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val pub = novel.publisher
                            if (!pub.isNullOrBlank()) {
                                val pubLabel = stringResource(Res.string.novel_publisher)
                                Text(
                                    text = "$pubLabel: $pub",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val statusText = try {
                                    NovelStatusEnum.valueOf(novel.status).meaning
                                } catch (e: Exception) {
                                    novel.status
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text(statusText) }
                                )
                                if (novel.totalVolumes > 0) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${novel.totalVolumes} V") }
                                    )
                                }
                            }
                            val desc = novel.description?.replace(Regex("(\\r?\\n)+"), "\n")?.trim()
                            if (!desc.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                val bId = novel.bgmId
                                if (bId != null) {
                                    IconButton(onClick = {
                                        appState.scope.launch {
                                            appState.api.novel.importNovelFromBangumi(bId, isUpdate = true).onSuccessData(appState, onSuccess = {
                                                loadNovels()
                                            })
                                        }
                                    }) {
                                        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh))
                                    }
                                }
                                IconButton(onClick = { openCreateDialog(novel) }) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.update))
                                }
                                IconButton(onClick = {
                                    selectedNovel.value = novel
                                    deleteDialogVisible.value = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 新建 / 编辑弹窗
    FormDialog(
        formDialogVisible = createDialogVisible,
        onConfirmation = {
            appState.scope.launch {
                val dto = NovelCreateOrUpdateDto(
                    id = selectedNovel.value?.id,
                    name = formName.value,
                    originalName = formOriginalName.value.ifBlank { null },
                    author = formAuthor.value.ifBlank { null },
                    illustrator = formIllustrator.value.ifBlank { null },
                    publisher = formPublisher.value.ifBlank { null },
                    description = formDescription.value.ifBlank { null },
                    status = formStatus.value
                )
                if (selectedNovel.value == null) {
                    appState.api.novel.createNovel(dto).onSuccessData(appState, onSuccess = {
                        createDialogVisible.value = false
                        loadNovels()
                    })
                } else {
                    appState.api.novel.updateNovel(dto).onSuccessData(appState, onSuccess = {
                        createDialogVisible.value = false
                        loadNovels()
                    })
                }
            }
        }
    ) {
        OutlinedTextField(
            value = formName.value,
            onValueChange = { formName.value = it },
            label = { Text(stringResource(Res.string.novel_name) + " *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formOriginalName.value,
            onValueChange = { formOriginalName.value = it },
            label = { Text(stringResource(Res.string.novel_original_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formAuthor.value,
            onValueChange = { formAuthor.value = it },
            label = { Text(stringResource(Res.string.novel_author)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formIllustrator.value,
            onValueChange = { formIllustrator.value = it },
            label = { Text(stringResource(Res.string.novel_illustrator)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formPublisher.value,
            onValueChange = { formPublisher.value = it },
            label = { Text(stringResource(Res.string.novel_publisher)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formDescription.value,
            onValueChange = { formDescription.value = it },
            label = { Text(stringResource(Res.string.novel_description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }

    // Bangumi 导入弹窗
    FormDialog(
        formDialogVisible = bgmImportDialogVisible,
        onConfirmation = {
            val bgmId = bgmIdInput.value.toULongOrNull()
            if (bgmId != null) {
                appState.scope.launch {
                    appState.api.novel.importNovelFromBangumi(bgmId).onSuccessData(appState, onSuccess = {
                        bgmImportDialogVisible.value = false
                        bgmIdInput.value = ""
                        loadNovels()
                    })
                }
            }
        }
    ) {
        Text(stringResource(Res.string.novel_bangumi_import_tip))
        OutlinedTextField(
            value = bgmIdInput.value,
            onValueChange = { bgmIdInput.value = it },
            label = { Text("Bangumi Subject ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = bgmSearchQuery.value,
                onValueChange = { bgmSearchQuery.value = it },
                label = { Text(stringResource(Res.string.novel_bangumi_search_label)) },
                modifier = Modifier.weight(1f),
                trailingIcon = if (bgmSearchQuery.value.isNotEmpty()) {
                    {
                        IconButton(onClick = {
                            bgmSearchQuery.value = ""
                            bgmSearchResults.value = emptyList()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                } else null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (bgmSearchQuery.value.isNotBlank()) {
                    bgmSearching.value = true
                    appState.scope.launch {
                        appState.api.novel.searchBangumiNovel(bgmSearchQuery.value).onSuccessData(appState, onSuccess = { results ->
                            bgmSearchResults.value = results
                            bgmSearching.value = false
                        }, onError = { bgmSearching.value = false })
                    }
                }
            }) {
                Text(stringResource(Res.string.search))
            }
        }
        if (bgmSearching.value) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        bgmSearchResults.value.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = {
                    val bId = item.bgmId
                    if (bId != null) {
                        appState.scope.launch {
                            appState.api.novel.importNovelFromBangumi(bId).onSuccessData(appState, onSuccess = {
                                bgmImportDialogVisible.value = false
                                bgmIdInput.value = ""
                                loadNovels()
                            })
                        }
                    }
                }
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    val authorLabel = stringResource(Res.string.novel_author)
                    Text("$authorLabel: ${item.author ?: "-"} | ID: ${item.bgmId}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    // 删除弹窗
    AlertDialog(
        alertDialogVisible = deleteDialogVisible,
        onConfirmation = {
            selectedNovel.value?.let { novel ->
                appState.scope.launch {
                    appState.api.novel.deleteNovel(novel.id).onSuccessData(appState, onSuccess = {
                        deleteDialogVisible.value = false
                        selectedNovel.value = null
                        loadNovels()
                    })
                }
            }
        },
        dialogTitle = stringResource(Res.string.delete) + ": ${selectedNovel.value?.name}"
    )
}
