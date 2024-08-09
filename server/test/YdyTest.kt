package dev.sunriseydy.acgn

import dev.sunriseydy.acgn.common.dto.AppInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

/**
 * @author SunriseYDY
 * @date 2024-07-02 15:38
 */
class YdyTest {
    @Test
    fun test() {
        println(
            Json.encodeToString(AppInfo(version = KotlinVersion(1, 2, 3)))
        )
    }
}