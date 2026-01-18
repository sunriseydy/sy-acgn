package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig
import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.ktor.server.application.*
import io.ktor.server.config.yaml.*

/**
 * @author SunriseYDY
 * @date 2024-07-10 21:32
 */
fun Application.configureLocalization() {
    LocalizationTool.loadLocalizations(CommonModuleAppConfig.AppLanguage.configValue)
}

fun LocalizationTool.loadLocalizations(language: Language = DEFAULT_LANGUAGE) {
    currentLanguage = language
    val yamlConfig =
        YamlConfig("data/localization/${currentLanguage.languageCode}-${currentLanguage.regionalCode}.yaml") ?: return
    yamlConfig.keys().forEach {
        putLocalization(it, yamlConfig.property(it).getString())
    }
}