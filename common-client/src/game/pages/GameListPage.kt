package dev.sunriseydy.acgn.client.game.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
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
import dev.sunriseydy.acgn.client.base.components.showMessage
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
    var refreshTargetGame by remember { mutableStateOf<Game?>(null) }

    val loadGames: (fromDb: Boolean) -> Unit = { fromDb ->
        appState.scope.launch {
            isLoading = true
            appState.api.game.getGameList(
                fromDb = fromDb,
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

    fun updateFromBangumi(game: Game) {
        val bgmId = game.bgmId ?: return
        appState.scope.launch {
            appState.showMessage("正在从 Bangumi 更新游戏数据...")
            appState.api.game.importFromBangumi(bgmId, isUpdate = true).onSuccessData(
                appState = appState,
                onSuccess = {
                    appState.showMessage("从 Bangumi 更新成功")
                    loadGames(false)
                }
            )
        }
    }

    fun updateFromSteam(game: Game) {
        val steamId = game.steamId ?: return
        appState.scope.launch {
            appState.showMessage("正在从 Steam 更新游戏数据...")
            appState.api.game.importFromSteam(steamId, isUpdate = true).onSuccessData(
                appState = appState,
                onSuccess = {
                    appState.showMessage("从 Steam 更新成功")
                    loadGames(false)
                }
            )
        }
    }

    val onRefreshGame: (Game) -> Unit = { game ->
        when {
            game.bgmId != null && game.steamId != null -> {
                refreshTargetGame = game
            }
            game.bgmId != null -> {
                updateFromBangumi(game)
            }
            game.steamId != null -> {
                updateFromSteam(game)
            }
            else -> {
                appState.showMessage("该游戏未关联 Bangumi 或 Steam ID，无法更新")
            }
        }
    }

    LaunchedEffect(searchKeyword, selectedPlatform, selectedPlayStatus) {
        loadGames(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("游戏库", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { loadGames(true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新（从数据库查询）")
                    }
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
                            },
                            onRefresh = {
                                onRefreshGame(game)
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
                loadGames(false)
            }
        )
    }

    if (refreshTargetGame != null) {
        val game = refreshTargetGame!!
        AlertDialog(
            onDismissRequest = { refreshTargetGame = null },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            title = { Text("选择更新来源") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("游戏《${game.name}》同时关联了 Bangumi 与 Steam，请选择数据更新来源：")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElevatedButton(
                            onClick = {
                                val target = refreshTargetGame
                                refreshTargetGame = null
                                target?.let { updateFromBangumi(it) }
                            }
                        ) {
                            Text("Bangumi")
                        }
                        ElevatedButton(
                            onClick = {
                                val target = refreshTargetGame
                                refreshTargetGame = null
                                target?.let { updateFromSteam(it) }
                            }
                        ) {
                            Text("Steam")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { refreshTargetGame = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit,
    onRefresh: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRefresh
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新游戏数据"
                    )
                }
            }

            val origName = game.originalName
            if (!origName.isNullOrBlank()) {
                Text(
                    text = origName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
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
