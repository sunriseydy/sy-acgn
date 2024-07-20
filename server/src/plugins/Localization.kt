package dev.sunriseydy.acgn.plugins

import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.tools.LocalizationTool
import io.ktor.server.application.Application
import io.ktor.server.config.yaml.YamlConfig

/**
 * @author SunriseYDY
 * @date 2024-07-10 21:32
 */
fun Application.configureLocalization() {
    LocalizationTool.loadLocalizations()
}

fun LocalizationTool.loadLocalizations() = loadLocalizations(DEFAULT_LANGUAGE)

fun LocalizationTool.loadLocalizations(language: Language) {
    currentLanguage = language
    val yamlConfig = YamlConfig("data/localization/${currentLanguage.code}.yaml") ?: return
    yamlConfig.keys().forEach {
        putLocalization(it, yamlConfig.property(it).getString())
    }
}