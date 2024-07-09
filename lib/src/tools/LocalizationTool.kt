package dev.sunriseydy.acgn.tools

import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.enums.Localizable
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlMap
import nl.adaptivity.xmlutil.core.impl.multiplatform.name


/**
 * @author SunriseYDY
 * @date 2024-07-09 14:56
 */

object LocalizationTool {
    internal val DEFAULT_LANGUAGE = Language.CHINESE

    fun getLocalizationMessage(key: String): String {
        return this.getLocalizationMessage(DEFAULT_LANGUAGE, key)
    }

    fun getLocalizationMessage(language: Language, key: String): String {
        return this.getLocalizationYaml(language).getStringOrNull(key) ?: key
    }

    fun getKeyFromEnum(enum: Any): String {
        if (enum is Enum && enum is Localizable) {
            return "enum.${enum.moduleName.name}.${enum.enumName}.${enum.name}"
        }
    }

    private fun getLocalizationYaml(language: Language): YamlMap {
        val resource = Thread.currentThread().contextClassLoader.getResource("localization/${language.code}.yaml")
        if (resource != null) {
            return resource.openStream().use {
                Yaml.decodeYamlMapFromString(String(it.readBytes()))
            }
        }
        return YamlMap(emptyMap())
    }
}
