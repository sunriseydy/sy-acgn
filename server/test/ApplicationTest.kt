package dev.sunriseydy.acgn.server

import dev.sunriseydy.acgn.anime.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.server.base.plugins.loadAppConfigFromFile
import dev.sunriseydy.acgn.tools.AppConfigTool
import io.ktor.server.config.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun testMediaTargetDirectoryConfigKey() {
        assertEquals("config.ANIME.MediaTargetDirectory", AnimeModuleAppConfig.MediaTargetDirectory.configKey)
    }

    @Test
    fun testLoadAppConfigFromFileIncludesAllKeys() {
        val config = ApplicationConfig("application.yaml")
        AppConfigTool.loadAppConfigFromFile(config)
        val configs = AppConfigTool.getAppConfigs()
        assertTrue(configs.containsKey("config.ANIME.MediaTargetDirectory"))
        assertTrue(configs.containsKey("config.ANIME.TmdbApiKey"))
        assertTrue(configs.containsKey("config.ANIME.BgmUserAgent"))
    }
}
