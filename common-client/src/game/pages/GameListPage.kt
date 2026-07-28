package dev.sunriseydy.acgn.client.game.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.client.base.navigation.GameDetailRoute
import dev.sunriseydy.acgn.game.dto.Game
import dev.sunriseydy.acgn.game.enums.GamePlayStatusEnum
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListPage(appState: AppState) {
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchKeyword by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf<String?>(null) }
    var selectedPlayStatus by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val loadGames: () -> Unit = {
        appState.scope.launch {
            isLoading = true
            appState.api.game.getGameList(
                name = searchKeyword.ifBlank { null },
                platform = selectedPlatform,
                playStatus = selectedPlayStatus
            ).onSuccessData(
                appState = appState,
                onSuccess = { data -> games = data }
            )
            isLoading = false
        }
    }

    LaunchedEffect(searchKeyword, selectedPlatform, selectedPlayStatus) {
        loadGames()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("游戏库", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "导入/新建游戏")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 搜索与筛选栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("搜索游戏名称/原名...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
            }

            // 状态 FilterChips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPlayStatus == null,
                    onClick = { selectedPlayStatus = null },
                    label = { Text("全部状态") }
                )
                GamePlayStatusEnum.entries.forEach { status ->
                    FilterChip(
                        selected = selectedPlayStatus == status.name,
                        onClick = {
                            selectedPlayStatus = if (selectedPlayStatus == status.name) null else status.name
                        },
                        label = { Text(status.meaning) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (games.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无游戏数据，点击右上角加号引入游戏", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 240.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(games, key = { it.id }) { game ->
                        GameCard(
                            game = game,
                            onClick = {
                                appState.navigationAction.add(GameDetailRoute(game.id))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        GameImportDialog(
            appState = appState,
            onDismiss = { showImportDialog = false },
            onImportSuccess = {
                showImportDialog = false
                loadGames()
            }
        )
    }
}

@Composable
fun GameCard(game: Game, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val origName = game.originalName
            if (!origName.isNullOrBlank()) {
                Text(
                    text = origName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val devName = game.developer
            if (!devName.isNullOrBlank()) {
                Text(
                    text = "开发: $devName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // 游玩记录
            val record = game.playRecord
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record != null) {
                    val statusEnum = runCatching { GamePlayStatusEnum.valueOf(record.playStatus) }.getOrNull()
                    val statusText = statusEnum?.meaning ?: record.playStatus
                    val hours = record.playTimeMinutes / 60.0

                    AssistChip(
                        onClick = { },
                        label = { Text(statusText, style = MaterialTheme.typography.labelSmall) }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "时长: %.1fh".format(hours),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "通关: ${record.clearCount}周目",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    AssistChip(
                        onClick = { },
                        label = { Text("未记录", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
