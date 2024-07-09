package dev.sunriseydy.acgn

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test

/**
 * @author SunriseYDY
 * @date 2024-07-02 15:38
 */
class YdyTest {
    @Test
    fun test() {
        val s = "Sat, 22 Jun 2024 22:27:36 -0700"
        println(s)
        val format = DateTimeComponents.Formats.RFC_1123
        val d = format.parse(s)
        println(d.toInstantUsingOffset().toLocalDateTime(TimeZone.currentSystemDefault()))
    }
}