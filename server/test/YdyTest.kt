package dev.sunriseydy.acgn

import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import dev.sunriseydy.acgn.tools.LocalizationTool
import kotlin.test.Test

/**
 * @author SunriseYDY
 * @date 2024-07-02 15:38
 */
class YdyTest {
    @Test
    fun test() {
        val s = LocalizationTool.getKeyFromEnum(AnimeAssociatedType.ANIME_SEASON)
        println(LocalizationTool.getLocalizationMessage(s))
    }
}