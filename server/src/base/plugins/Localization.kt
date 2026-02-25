package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.common.config.CommonModuleAppConfig
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
    val yamlConfig =
        YamlConfig("localization/${language.languageCode}-${language.regionalCode}.yaml") ?: return
    val loaded = buildMap {
        yamlConfig.keys().forEach {
            put(it, yamlConfig.property(it).getString())
        }
    }
    replaceAll(language, loaded)
}
