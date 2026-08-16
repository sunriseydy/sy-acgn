package dev.sunriseydy.acgn.client.anime.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.anime.components.CreateAnimeSeason
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService
import dev.sunriseydy.acgn.client.base.components.AttachImage
import dev.sunriseydy.acgn.client.base.components.PageTitle
import dev.sunriseydy.acgn.client.base.navigation.AnimeSeasonDetailRoute
import dev.sunriseydy.acgn.client.base.navigation.TopLevelRouteEnum
import dev.sunriseydy.acgn.client.res.*
import dev.sunriseydy.acgn.tools.i
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.resources.stringResource

private val logger = KotlinLogging.logger { }

/**
 * 动画季度列表页
 *
 * @author SunriseYDY
 * @date 2024-08-15 16:08
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AnimeSeason(appState: AppState) {
    val sectionMapState: MutableState<MutableMap<String, List<AnimeSeason>>> =
        remember { mutableStateOf(mutableMapOf()) }
    val state: LazyStaggeredGridState = rememberLazyStaggeredGridState()
    val loading = remember { mutableStateOf(false) }
    val createDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val searchName = remember { mutableStateOf("") }
    val animeSeasonService = remember(appState) { AnimeSeasonService(appState) }

    fun loadData(fromDb: Boolean = false) {
        if (!loading.value) {
            loading.value = true
            animeSeasonService.loadData(
                name = searchName.value,
                fromDb = fromDb,
                onSuccess = {
                    sectionMapState.value = it
                    loading.value = false
                },
                onError = {
                    loading.value = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PageTitle(TopLevelRouteEnum.ANIME_SEASON.meaning) {
            OutlinedTextField(
                value = searchName.value,
                onValueChange = { searchName.value = it },
                label = { Text(stringResource(Res.string.search)) },
                singleLine = true,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = {
                loadData()
            }) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = {
                searchName.value = ""
                loadData(fromDb = true)
            }) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(48.dp))
            }
            IconButton(onClick = {
                createDialogVisible.value = true
            }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(48.dp))
            }
        }
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(8.dp),
        ) {
            sectionMapState.value.forEach { sectionMap ->
                val (title, seasons) = sectionMap
                item(span = StaggeredGridItemSpan.FullLine) {
                    PageTitle(title)
                }
                items(seasons) { season ->
                    Card(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        onClick = {
                            appState.navigationAction.add(AnimeSeasonDetailRoute(season.id))
                        }
                    ) {
                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val posterId = season.posterId
                                if (!posterId.isNullOrBlank()) {
                                    AttachImage(
                                        appState = appState,
                                        attachId = posterId,
                                        modifier = Modifier.padding(end = 8.dp).width(300.dp).height(400.dp),
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                            SelectionContainer {
                                Text(
                                    text = "${season.anime?.name ?: ""} - ${season.name}",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("第 ${season.season} 季") }
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text("共 ${season.numberOfEpisodes} 集") }
                                )
                                AnimeAdditionType.FileStatus.additionalInfo(season.additions)?.also {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(i(it.additionalValue)) }
                                    )
                                }
                            }
                            Text(
                                text = season.description?.takeUnless { it.isBlank() }
                                    ?: season.anime?.description.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    CreateAnimeSeason(animeSeasonService, createDialogVisible, onSuccess = { loadData() })
}
