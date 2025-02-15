package dev.sunriseydy.acgn.client.anime.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.sunriseydy.acgn.client.anime.service.AnimeSeasonService

/**
 * @author SunriseYDY
 * @date 2025-02-15 12:46
 */
@Composable
fun CreateAnimeSeasonFromFile(
    animeSeasonService: AnimeSeasonService,
    fileCreateDialogVisible: MutableState<Boolean>,
) {
    val createDialogVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    
}