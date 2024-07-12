package dev.sunriseydy.acgn

import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig
import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.exception.CommonModuleException
import dev.sunriseydy.acgn.plugins.loadLocalizations
import dev.sunriseydy.acgn.tools.LocalizationTool
import kotlin.test.Test

/**
 * @author SunriseYDY
 * @date 2024-07-02 15:38
 */
class YdyTest {
    @Test
    fun test() {
        LocalizationTool.loadLocalizations()
        println(CommonModuleAppConfig.AppName.configValue)
        println(CommonModuleAppConfig.AppName.localization)
        println(CommonModuleAppConfig.AppName.description)
        println(Language.SIMPLIFIED_CHINESE.localization)
        println(CommonModuleException("test").message)
    }
}