package dev.sunriseydy.acgn.client.game.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccess
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.game.dto.Game
import dev.sunriseydy.acgn.game.dto.GameCreateOrUpdateDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameImportDialog(
    appState: AppState,
    onDismiss: () -> Unit,
    onImportSuccess: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Game>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // 手动新建表单
    var manualName by remember { mutableStateOf("") }
    var manualOriginalName by remember { mutableStateOf("") }
    var manualDev by remember { mutableStateOf("") }
    var manualPub by remember { mutableStateOf("") }
    var manualDesc by remember { mutableStateOf("") }

    val handleSearch = {
        if (searchQuery.isNotBlank()) {
            appState.scope.launch {
                isSearching = true
                if (selectedTab == 0) {
                    // Bangumi
                    appState.api.game.searchBangumiGame(searchQuery).onSuccessData(
                        appState = appState,
                        onSuccess = { list -> searchResults = list }
                    )
                } else if (selectedTab == 1) {
                    // Steam
                    appState.api.game.searchSteamGame(searchQuery).onSuccessData(
                        appState = appState,
                        onSuccess = { list -> searchResults = list }
                    )
                }
                isSearching = false
            }
        }
    }

    val importGame: (Game) -> Unit = { game ->
        appState.scope.launch {
            if (selectedTab == 0 && game.bgmId != null) {
                appState.api.game.importFromBangumi(game.bgmId!!).onSuccess(appState) {
                    onImportSuccess()
                }
            } else if (selectedTab == 1 && game.steamId != null) {
                appState.api.game.importFromSteam(game.steamId!!).onSuccess(appState) {
                    onImportSuccess()
                }
            }
        }
    }

    val createManualGame = {
        if (manualName.isNotBlank()) {
            appState.scope.launch {
                val dto = GameCreateOrUpdateDto(
                    name = manualName,
                    originalName = manualOriginalName.ifBlank { null },
                    developer = manualDev.ifBlank { null },
                    publisher = manualPub.ifBlank { null },
                    description = manualDesc.ifBlank { null }
                )
                appState.api.game.createGame(dto).onSuccess(appState) {
                    onImportSuccess()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加 / 导入游戏") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; searchResults = emptyList() }) {
                        Text("Bangumi 导入", modifier = Modifier.padding(12.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; searchResults = emptyList() }) {
                        Text("Steam 导入", modifier = Modifier.padding(12.dp))
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("手动新建", modifier = Modifier.padding(12.dp))
                    }
                }

                if (selectedTab == 0 || selectedTab == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(if (selectedTab == 0) "搜索 Bangumi 游戏..." else "搜索 Steam 游戏...") },
                            singleLine = true
                        )
                        Button(onClick = { handleSearch() }) {
                            Text("搜索")
                        }
                    }

                    if (isSearching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { game ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(game.name, fontWeight = FontWeight.Bold)
                                            val orig = game.originalName
                                            if (!orig.isNullOrBlank()) {
                                                Text(orig, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        Button(onClick = { importGame(game) }) {
                                            Text("导入")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 手动新建
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualName,
                            onValueChange = { manualName = it },
                            label = { Text("游戏名称 *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = manualOriginalName,
                            onValueChange = { manualOriginalName = it },
                            label = { Text("原名 / 外文名") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = manualDev,
                            onValueChange = { manualDev = it },
                            label = { Text("开发商 / 社团") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = manualPub,
                            onValueChange = { manualPub = it },
                            label = { Text("发行商") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 2) {
                Button(onClick = { createManualGame() }) {
                    Text("创建游戏")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
