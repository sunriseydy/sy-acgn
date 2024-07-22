package dev.sunriseydy.acgn

import dev.sunriseydy.acgn.anime.tools.FileTool
import kotlin.test.Test

/**
 * @author SunriseYDY
 * @date 2024-07-02 15:38
 */
class YdyTest {
    @Test
    fun test() {
        FileTool().listVideoNames("/media/sunriseydy/DATA/下载/Anime/[VCB-Studio] NieR Automata Ver1.1a [Ma10p_1080p]")
            .forEach(::println)
    }
}