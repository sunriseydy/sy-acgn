package dev.sunriseydy.acgn.client.novel.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.client.base.components.AlertDialog
import dev.sunriseydy.acgn.client.base.components.AttachImage
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.base.components.PageTitle
import dev.sunriseydy.acgn.client.res.*
import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelVolume
import dev.sunriseydy.acgn.novel.dto.NovelVolumeCreateOrUpdateDto
import dev.sunriseydy.acgn.novel.enums.NovelStatusEnum
import dev.sunriseydy.acgn.novel.enums.ReadingStatusEnum
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun NovelDetailPage(appState: AppState, novelId: ULong) {
    val novelState = remember { mutableStateOf<Novel?>(null) }
    val loading = remember { mutableStateOf(false) }

    // Dialog states
    val createVolumeDialogVisible = remember { mutableStateOf(false) }
    val deleteVolumeDialogVisible = remember { mutableStateOf(false) }
    val selectedVolume = remember { mutableStateOf<NovelVolume?>(null) }

    // Form fields for volume
    val formVolNumber = remember { mutableStateOf("1.0") }
    val formVolName = remember { mutableStateOf("") }
    val formVolDescription = remember { mutableStateOf("") }
    val formVolIsbn = remember { mutableStateOf("") }

    fun loadNovelDetail() {
        if (loading.value) return
        loading.value = true
        appState.scope.launch {
            appState.api.novel.getNovelById(novelId).onSuccessData(appState, onSuccess = { novel ->
                novelState.value = novel
                loading.value = false
            })
        }
    }

    LaunchedEffect(novelId) {
        loadNovelDetail()
    }

    fun openVolumeDialog(volume: NovelVolume? = null) {
        selectedVolume.value = volume
        formVolNumber.value = volume?.volumeNumber?.toString() ?: ( (novelState.value?.volumes?.size ?: 0) + 1 ).toDouble().toString()
        formVolName.value = volume?.name.orEmpty()
        formVolDescription.value = volume?.description.orEmpty()
        formVolIsbn.value = volume?.isbn.orEmpty()
        createVolumeDialogVisible.value = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(novelState.value?.name ?: stringResource(Res.string.novel_detail_title)) {
            IconButton(onClick = { appState.navigationAction.removeLast() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            IconButton(onClick = { loadNovelDetail() }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh))
            }
            IconButton(onClick = { openVolumeDialog() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.novel_add_volume))
            }
        }

        val novel = novelState.value
        if (loading.value || novel == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val posterId = novel.posterId
                            if (!posterId.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    AttachImage(
                                        appState = appState,
                                        attachId = posterId,
                                        modifier = Modifier.width(180.dp).height(240.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            SelectionContainer {
                                Text(novel.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            val origName = novel.originalName
                            if (!origName.isNullOrBlank()) {
                                Text(origName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val authorLabel = stringResource(Res.string.novel_author)
                            val illustratorLabel = stringResource(Res.string.novel_illustrator)
                            val publisherLabel = stringResource(Res.string.novel_publisher)
                            Text("$authorLabel: ${novel.author ?: "-"} | $illustratorLabel: ${novel.illustrator ?: "-"} | $publisherLabel: ${novel.publisher ?: "-"}")

                            val statusText = try {
                                NovelStatusEnum.valueOf(novel.status).meaning
                            } catch (e: Exception) {
                                novel.status
                            }
                            val statusLabel = stringResource(Res.string.novel_status)
                            val totalVolLabel = stringResource(Res.string.novel_total_volumes)
                            Text("$statusLabel: $statusText | $totalVolLabel: ${novel.volumes.size}")

                            val desc = novel.description?.replace(Regex("(\\r?\\n){3,}"), "\n\n")?.trim()
                            if (!desc.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(desc, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Volume list title
                item {
                    Text(stringResource(Res.string.novel_volume_list), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }

                if (novel.volumes.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(stringResource(Res.string.novel_no_volume_data))
                            }
                        }
                    }
                } else {
                    items(novel.volumes) { volume ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val volPosterId = volume.posterId
                                if (!volPosterId.isNullOrBlank()) {
                                    AttachImage(
                                        appState = appState,
                                        attachId = volPosterId,
                                        modifier = Modifier.padding(end = 12.dp).width(60.dp).height(80.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "V${volume.volumeNumber} - ${volume.name}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    val isbn = volume.isbn
                                    if (!isbn.isNullOrBlank()) {
                                        Text("ISBN: $isbn", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (volume.releaseDate != null) {
                                        Text("${volume.releaseDate}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    val volDesc = volume.description?.replace(Regex("(\\r?\\n)+"), "\n")?.trim()
                                    if (!volDesc.isNullOrBlank()) {
                                        Text(volDesc, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Reading status selector
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    val readingStatusText = try {
                                        ReadingStatusEnum.valueOf(volume.readingStatus).meaning
                                    } catch (e: Exception) {
                                        volume.readingStatus
                                    }
                                    FilterChip(
                                        selected = true,
                                        onClick = { expanded = true },
                                        label = { Text(readingStatusText) }
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        ReadingStatusEnum.entries.forEach { status ->
                                            DropdownMenuItem(
                                                text = { Text(status.meaning) },
                                                onClick = {
                                                    expanded = false
                                                    appState.scope.launch {
                                                        appState.api.novel.updateVolumeReadingStatus(volume.id, status.name).onSuccessData(appState, onSuccess = {
                                                            loadNovelDetail()
                                                        })
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { openVolumeDialog(volume) }) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.update))
                                }
                                IconButton(onClick = {
                                    selectedVolume.value = volume
                                    deleteVolumeDialogVisible.value = true
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

    // Add / Edit Volume Dialog
    FormDialog(
        formDialogVisible = createVolumeDialogVisible,
        onConfirmation = {
            val volNum = formVolNumber.value.toDoubleOrNull() ?: 1.0
            appState.scope.launch {
                val dto = NovelVolumeCreateOrUpdateDto(
                    id = selectedVolume.value?.id,
                    novelId = novelId,
                    volumeNumber = volNum,
                    name = formVolName.value,
                    description = formVolDescription.value.ifBlank { null },
                    isbn = formVolIsbn.value.ifBlank { null }
                )
                if (selectedVolume.value == null) {
                    appState.api.novel.createVolume(dto).onSuccessData(appState, onSuccess = {
                        createVolumeDialogVisible.value = false
                        loadNovelDetail()
                    })
                } else {
                    appState.api.novel.updateVolume(dto).onSuccessData(appState, onSuccess = {
                        createVolumeDialogVisible.value = false
                        loadNovelDetail()
                    })
                }
            }
        }
    ) {
        OutlinedTextField(
            value = formVolNumber.value,
            onValueChange = { formVolNumber.value = it },
            label = { Text(stringResource(Res.string.novel_volume_number) + " *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formVolName.value,
            onValueChange = { formVolName.value = it },
            label = { Text(stringResource(Res.string.novel_volume_name) + " *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formVolIsbn.value,
            onValueChange = { formVolIsbn.value = it },
            label = { Text(stringResource(Res.string.novel_volume_isbn)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formVolDescription.value,
            onValueChange = { formVolDescription.value = it },
            label = { Text(stringResource(Res.string.novel_volume_description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }

    // Delete Volume Dialog
    AlertDialog(
        alertDialogVisible = deleteVolumeDialogVisible,
        onConfirmation = {
            selectedVolume.value?.let { volume ->
                appState.scope.launch {
                    appState.api.novel.deleteVolume(volume.id).onSuccessData(appState, onSuccess = {
                        deleteVolumeDialogVisible.value = false
                        selectedVolume.value = null
                        loadNovelDetail()
                    })
                }
            }
        },
        dialogTitle = stringResource(Res.string.delete) + ": V${selectedVolume.value?.volumeNumber} (${selectedVolume.value?.name})"
    )
}
