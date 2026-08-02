package dev.sunriseydy.acgn.client.game.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccess
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.client.base.components.AttachImage
import dev.sunriseydy.acgn.game.dto.*
import dev.sunriseydy.acgn.game.enums.GamePlatformEnum
import dev.sunriseydy.acgn.game.enums.GamePlayStatusEnum
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailPage(appState: AppState, gameId: ULong) {
    var gameData by remember { mutableStateOf<Game?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 游玩记录修改表单
    var playStatus by remember { mutableStateOf(GamePlayStatusEnum.UNPLAYED.name) }
    var playTimeHoursText by remember { mutableStateOf("0") }
    var clearCountText by remember { mutableStateOf("0") }
    var scoreText by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }

    val loadGame: () -> Unit = {
        appState.scope.launch {
            isLoading = true
            appState.api.game.getGameById(gameId).onSuccessData(
                appState = appState,
                onSuccess = { data ->
                    gameData = data
                    val rec = data.playRecord
                    if (rec != null) {
                        playStatus = rec.playStatus
                        playTimeHoursText = (rec.playTimeMinutes / 60.0).toString()
                        clearCountText = rec.clearCount.toString()
                        scoreText = rec.score?.toString() ?: ""
                        commentText = rec.comment ?: ""
                    }
                }
            )
            isLoading = false
        }
    }

    LaunchedEffect(gameId) {
        loadGame()
    }

    val savePlayRecord = {
        appState.scope.launch {
            val hours = playTimeHoursText.toDoubleOrNull() ?: 0.0
            val minutes = (hours * 60).toLong()
            val clearCount = clearCountText.toIntOrNull() ?: 0
            val score = scoreText.toDoubleOrNull()

            val dto = GamePlayRecordCreateOrUpdateDto(
                id = gameData?.playRecord?.id,
                gameId = gameId,
                playStatus = playStatus,
                playTimeMinutes = minutes,
                clearCount = clearCount,
                score = score,
                comment = commentText.ifBlank { null }
            )
            appState.api.game.updatePlayRecord(dto).onSuccess(appState) {
                loadGame()
            }
        }
    }

    val deleteGame = {
        appState.scope.launch {
            appState.api.game.deleteGame(gameId).onSuccess(appState) {
                appState.navigationAction.removeLast()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gameData?.name ?: "游戏详情") },
                navigationIcon = {
                    IconButton(onClick = { appState.navigationAction.removeLast() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { deleteGame() }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        val current = gameData
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (current == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("游戏数据不存在")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 基本信息卡片
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val posterId = current.posterId
                        if (!posterId.isNullOrBlank()) {
                            AttachImage(
                                appState = appState,
                                attachId = posterId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Text(current.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                        val origName = current.originalName
                        if (!origName.isNullOrBlank()) {
                            Text("原名: $origName", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            current.developer?.let { Text("开发商: $it", style = MaterialTheme.typography.bodyMedium) }
                            current.publisher?.let { Text("发行商: $it", style = MaterialTheme.typography.bodyMedium) }
                        }

                        current.releaseDate?.let { Text("首发日期: $it", style = MaterialTheme.typography.bodyMedium) }
                        current.rating?.let { Text("综合评分: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }

                        val desc = current.description
                        if (!desc.isNullOrBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text("游戏简介", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(desc, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // 游玩记录编辑卡片
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("游玩状态与记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        Text("游玩状态", style = MaterialTheme.typography.titleSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GamePlayStatusEnum.entries.forEach { status ->
                                FilterChip(
                                    selected = playStatus == status.name,
                                    onClick = { playStatus = status.name },
                                    label = { Text(status.meaning) }
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = playTimeHoursText,
                                onValueChange = { playTimeHoursText = it },
                                label = { Text("累计游玩时长 (小时)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = clearCountText,
                                onValueChange = { clearCountText = it },
                                label = { Text("通关周目/次数") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = scoreText,
                            onValueChange = { scoreText = it },
                            label = { Text("个人评分 (0-10)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("游玩评价 / 心得笔记") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Button(
                            onClick = { savePlayRecord() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("保存游玩记录")
                        }
                    }
                }

                // 发行平台列表卡片
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("发行平台与版本", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (current.releases.isEmpty()) {
                            Text("暂未登记发行平台", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            current.releases.forEach { rel ->
                                val platEnum = runCatching { GamePlatformEnum.valueOf(rel.platform) }.getOrNull()
                                val platName = platEnum?.meaning ?: rel.platform
                                ListItem(
                                    headlineContent = { Text(platName, fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("发售日: ${rel.releaseDate ?: "未知"} | 版本: ${rel.version ?: "标准版"}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
