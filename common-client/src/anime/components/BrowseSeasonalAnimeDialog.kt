package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.AttachImage
import dev.sunriseydy.acgn.client.base.components.FormDialog
import dev.sunriseydy.acgn.client.res.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

/**
 * Bangumi 年度-季度新番排期浏览及快速入库弹窗
 *
 * 允许用户选择年度、季度（冬/春/夏/秋）与排序方式，
 * 浏览当季新番条目，并可一键关联 TMDB 数据后添加进本地媒体库。
 *
 * @author SunriseYDY
 * @date 2026-10-18
 */
@Composable
fun BrowseSeasonalAnimeDialog(
    appState: AppState,
    animeSeasonService: AnimeSeasonService,
    visible: MutableState<Boolean>,
    existingSeasons: List<AnimeSeason> = emptyList(),
    onSeasonAdded: (AnimeSeason) -> Unit = { },
) {
    if (!visible.value) return

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val initialYear = today.year
    val initialMonthType = remember {
        when (today.month.number) {
            in 1..3 -> AnimeMonthType.WINTER
            in 4..6 -> AnimeMonthType.SPRING
            in 7..9 -> AnimeMonthType.SUMMER
            else -> AnimeMonthType.FALL
        }
    }

    val selectedYear = remember { mutableStateOf(initialYear) }
    val selectedMonthType = remember { mutableStateOf(initialMonthType) }
    val selectedSort = remember { mutableStateOf("heat") } // heat, score, rank
    val searchQuery = remember { mutableStateOf("") }

    val seasonalList = remember { mutableStateOf<List<AnimeSeason>>(emptyList()) }
    val totalCount = remember { mutableStateOf(0) }
    val loading = remember { mutableStateOf(false) }
    val currentOffset = remember { mutableStateOf(0) }
    val pageSize = 20

    // 已入库的 bgmId 集合
    val addedBgmIds = remember { mutableStateListOf<ULong>() }
    val existingBgmIds = remember(existingSeasons, addedBgmIds.toList()) {
        existingSeasons.mapNotNull { it.bgmId }.toSet() + addedBgmIds
    }

    // 选中待入库的条目（弹出二次确认/TMDB 匹配弹窗）
    val animeToConfirm = remember { mutableStateOf<AnimeSeason?>(null) }

    fun loadData(reset: Boolean = true) {
        if (loading.value) return
        loading.value = true
        val targetOffset = if (reset) 0 else currentOffset.value

        animeSeasonService.getSeasonalAnime(
            year = selectedYear.value,
            monthType = selectedMonthType.value,
            query = searchQuery.value.ifBlank { null },
            sort = selectedSort.value,
            limit = pageSize,
            offset = targetOffset,
            onSuccess = { res ->
                totalCount.value = res.total
                if (reset) {
                    seasonalList.value = res.items
                    currentOffset.value = res.items.size
                } else {
                    seasonalList.value = seasonalList.value + res.items
                    currentOffset.value = currentOffset.value + res.items.size
                }
                loading.value = false
            },
            onError = { errMsg ->
                loading.value = false
                appState.scope.launch {
                    appState.snackbarHostState.showSnackbar(errMsg)
                }
            }
        )
    }

    // 筛选条件变化时自动刷新
    LaunchedEffect(selectedYear.value, selectedMonthType.value, selectedSort.value) {
        loadData(reset = true)
    }

    Dialog(
        onDismissRequest = { visible.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                // 顶部标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.browse_seasonal_anime_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "当前检索：${selectedYear.value} 年 ${selectedMonthType.value.meaning} · 共 ${totalCount.value} 部动画",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { visible.value = false }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 筛选控制栏：年份选择、季度选择、排序方式、搜索框
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 年份步进
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 4.dp)
                    ) {
                        IconButton(onClick = { selectedYear.value-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一年")
                        }
                        Text(
                            text = "${selectedYear.value} 年",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(onClick = { selectedYear.value++ }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下一年")
                        }
                    }

                    // 季度选择 Chip
                    AnimeMonthType.entries.forEach { monthType ->
                        FilterChip(
                            selected = selectedMonthType.value == monthType,
                            onClick = { selectedMonthType.value = monthType },
                            label = { Text(monthType.meaning) }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 排序选项
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            selected = selectedSort.value == "heat",
                            onClick = { selectedSort.value = "heat" }
                        ) {
                            Text(stringResource(Res.string.seasonal_sort_heat))
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            selected = selectedSort.value == "score",
                            onClick = { selectedSort.value = "score" }
                        ) {
                            Text(stringResource(Res.string.seasonal_sort_score))
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            selected = selectedSort.value == "rank",
                            onClick = { selectedSort.value = "rank" }
                        ) {
                            Text(stringResource(Res.string.seasonal_sort_rank))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 关键词搜索栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("在当季新番中按标题过滤…") },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.value.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery.value = ""
                                    loadData(reset = true)
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { loadData(reset = true) })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { loadData(reset = true) }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(Res.string.search))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { loadData(reset = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 内容网格
                if (loading.value && seasonalList.value.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (seasonalList.value.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(Res.string.no_data),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 340.dp),
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(seasonalList.value, key = { it.bgmId ?: it.name }) { season ->
                            val isAdded = season.bgmId != null && existingBgmIds.contains(season.bgmId)
                            SeasonalAnimeCard(
                                appState = appState,
                                season = season,
                                isAdded = isAdded,
                                onAddClick = {
                                    animeToConfirm.value = season
                                }
                            )
                        }

                        // 加载更多按钮
                        if (seasonalList.value.size < totalCount.value) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (loading.value) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    } else {
                                        OutlinedButton(onClick = { loadData(reset = false) }) {
                                            Text("加载更多 (${seasonalList.value.size} / ${totalCount.value})")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 入库确认与 TMDB 自动匹配对话框
    animeToConfirm.value?.let { targetSeason ->
        SeasonalAnimeConfirmDialog(
            appState = appState,
            service = animeSeasonService,
            season = targetSeason,
            onDismiss = { animeToConfirm.value = null },
            onSuccess = { saved ->
                targetSeason.bgmId?.let { addedBgmIds.add(it) }
                onSeasonAdded(saved)
                animeToConfirm.value = null
                appState.scope.launch {
                    appState.snackbarHostState.showSnackbar(
                        "已成功将《${saved.name}》加入媒体库"
                    )
                }
            }
        )
    }
}

/**
 * 季度新番条目卡片
 */
@Composable
private fun SeasonalAnimeCard(
    appState: AppState,
    season: AnimeSeason,
    isAdded: Boolean,
    onAddClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        ) {
            // 封面图
            AttachImage(
                appState = appState,
                imageUrl = season.bgmCoverUrl.orEmpty(),
                modifier = Modifier
                    .width(96.dp)
                    .height(136.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f).heightIn(min = 136.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // 主标题（中文名）
                    Text(
                        text = season.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // 原名（日文名）
                    season.bgmOriginalName?.takeIf { it != season.name }?.let { origName ->
                        Text(
                            text = origName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 评分与标签信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        season.bgmScore?.takeIf { it > 0.0 }?.let { score ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text("★ $score", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                        season.bgmRank?.takeIf { it > 0 }?.let { rank ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text("#$rank", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                        if (season.numberOfEpisodes > 0) {
                            Text(
                                text = "${season.numberOfEpisodes} 话",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 放送日期
                    season.airDate?.let { date ->
                        Text(
                            text = "放送：$date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // 简述
                    season.description?.ifBlank { null }?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // 底部状态 / 入库按钮
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAdded) {
                        FilledTonalButton(
                            onClick = { },
                            enabled = false,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.already_in_library), style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Button(
                            onClick = onAddClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.add_to_library), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 入库确认及 TMDB 联动匹配弹窗
 */
@Composable
private fun SeasonalAnimeConfirmDialog(
    appState: AppState,
    service: AnimeSeasonService,
    season: AnimeSeason,
    onDismiss: () -> Unit,
    onSuccess: (AnimeSeason) -> Unit
) {
    val animeName = remember { mutableStateOf(season.name) }
    val seasonNumber = remember { mutableStateOf(season.season.toString()) }
    val isMatchTmdb = remember { mutableStateOf(true) }

    // TMDB 搜索与匹配状态
    val tmdbSearching = remember { mutableStateOf(false) }
    val tmdbCandidates = remember { mutableStateOf<List<Anime>>(emptyList()) }
    val selectedTmdbAnime = remember { mutableStateOf<Anime?>(null) }
    val tmdbSeasons = remember { mutableStateOf<List<AnimeSeason>>(emptyList()) }
    val selectedTmdbSeason = remember { mutableStateOf<AnimeSeason?>(null) }
    val isSaving = remember { mutableStateOf(false) }

    // 初始化时自动根据名称触发 TMDB 搜索
    LaunchedEffect(season) {
        tmdbSearching.value = true
        service.searchAnimeFromTMDB(
            name = season.name,
            onSuccess = { list ->
                tmdbCandidates.value = list
                tmdbSearching.value = false
                val first = list.firstOrNull()
                if (first != null) {
                    selectedTmdbAnime.value = first
                    // 获取对应剧集的季度列表
                    first.tmdbId?.let { showId ->
                        service.getAnimeByTmdbId(
                            id = showId,
                            onSuccess = { detail ->
                                tmdbSeasons.value = detail.animeSeasons
                                // 默认匹配与 season.season 相同序号的季，或第 1 季
                                val matched = detail.animeSeasons.firstOrNull { it.season == season.season }
                                    ?: detail.animeSeasons.firstOrNull { it.season > 0 }
                                selectedTmdbSeason.value = matched
                            }
                        )
                    }
                }
            },
            onError = {
                tmdbSearching.value = false
            }
        )
    }

    fun handleConfirm() {
        if (isSaving.value) return
        isSaving.value = true

        val seasonNum = seasonNumber.value.toIntOrNull() ?: season.season
        val chosenTmdbShow = if (isMatchTmdb.value) selectedTmdbAnime.value else null
        val chosenTmdbSeason = if (isMatchTmdb.value) selectedTmdbSeason.value else null

        // 组装最终待保存的 AnimeSeason
        val baseAnime = season.anime ?: Anime(
            id = ULong.MIN_VALUE,
            name = animeName.value,
            description = season.description,
            firstAirDate = season.airDate,
            bgmId = season.bgmId
        )

        val mergedAnime = if (chosenTmdbShow != null) {
            baseAnime.copy(
                name = animeName.value,
                tmdbId = chosenTmdbShow.tmdbId,
                bgmId = season.bgmId
            )
        } else {
            baseAnime.copy(name = animeName.value)
        }

        val targetSeason = season.copy(
            name = animeName.value,
            season = seasonNum,
            tmdbId = chosenTmdbSeason?.tmdbId,
            bgmId = season.bgmId,
            anime = mergedAnime
        )

        service.saveAnimeSeason(
            animeSeason = targetSeason,
            onSuccess = { saved ->
                // 若关联了 TMDB，则触发集数同步
                if (saved.anime?.tmdbId != null || chosenTmdbShow?.tmdbId != null) {
                    service.syncEpisodes(
                        seasonId = saved.id,
                        onSuccess = {
                            isSaving.value = false
                            onSuccess(saved)
                        },
                        onError = {
                            isSaving.value = false
                            onSuccess(saved)
                        }
                    )
                } else {
                    isSaving.value = false
                    onSuccess(saved)
                }
            },
            onError = { err ->
                isSaving.value = false
                appState.scope.launch {
                    appState.snackbarHostState.showSnackbar("入库失败: $err")
                }
            }
        )
    }

    FormDialog(
        formDialogVisible = remember { mutableStateOf(true) },
        onDismissRequest = onDismiss,
        onConfirmation = { handleConfirm() },
        confirmationText = if (isSaving.value) "保存中…" else stringResource(Res.string.confirm_create_seasonal_anime)
    ) {
        val fieldWidth = 480.dp
        Column(
            modifier = Modifier.width(fieldWidth).verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "添加入库：${season.name}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 基本信息表单
            OutlinedTextField(
                value = animeName.value,
                onValueChange = { animeName.value = it },
                label = { Text("动画名称") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = seasonNumber.value,
                    onValueChange = { seasonNumber.value = it },
                    label = { Text("季度序号") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = "${season.year} 年 ${season.month} 月",
                    onValueChange = { },
                    label = { Text("放送年月") },
                    readOnly = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TMDB 联动选项
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMatchTmdb.value = !isMatchTmdb.value }
            ) {
                Checkbox(
                    checked = isMatchTmdb.value,
                    onCheckedChange = { isMatchTmdb.value = it }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.match_tmdb_title),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isMatchTmdb.value) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (tmdbSearching.value) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("正在从 TMDB 匹配影视条目…", style = MaterialTheme.typography.bodySmall)
                            }
                        } else if (tmdbCandidates.value.isEmpty()) {
                            Text(
                                text = "未能在 TMDB 自动匹配到同名影视条目，入库时将仅保存 Bangumi 基础元数据。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("已从 TMDB 找到候选条目：", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(6.dp))

                            // 候选下拉菜单或选择
                            tmdbCandidates.value.take(3).forEach { tmdbShow ->
                                val isSelected = selectedTmdbAnime.value?.id == tmdbShow.id || selectedTmdbAnime.value?.tmdbId == tmdbShow.tmdbId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTmdbAnime.value = tmdbShow
                                            tmdbShow.tmdbId?.let { showId ->
                                                service.getAnimeByTmdbId(
                                                    id = showId,
                                                    onSuccess = { detail ->
                                                        tmdbSeasons.value = detail.animeSeasons
                                                        selectedTmdbSeason.value = detail.animeSeasons.firstOrNull { it.season == season.season }
                                                            ?: detail.animeSeasons.firstOrNull { it.season > 0 }
                                                    }
                                                )
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${tmdbShow.name} (${tmdbShow.firstAirDate?.year ?: "-"})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 关联的 TMDB 季度选择
                            if (tmdbSeasons.value.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("对应 TMDB 季度：", style = MaterialTheme.typography.labelSmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    tmdbSeasons.value.filter { it.season > 0 }.forEach { tmdbSeason ->
                                        FilterChip(
                                            selected = selectedTmdbSeason.value?.season == tmdbSeason.season,
                                            onClick = { selectedTmdbSeason.value = tmdbSeason },
                                            label = { Text("第 ${tmdbSeason.season} 季") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
