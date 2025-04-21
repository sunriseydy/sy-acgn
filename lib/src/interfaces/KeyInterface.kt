package dev.sunriseydy.acgn.interfaces

import dev.sunriseydy.acgn.enums.ModuleName
import dev.sunriseydy.acgn.tools.LocalizationTool

/**
 * @author SunriseYDY
 * @date 2025-04-20 23:53
 */
interface Key {
    val moduleName: ModuleName
    val key: String
    val meaning: String get() = LocalizationTool.getLocalization(this.key)
}

interface EnumKey : Key {
    override val key: String get() = this.getEnumKey(this)
}

interface Message : Key {
    val level: String
    override val key: String
        get() = this.getMessageKey(this)
}

interface ErrorMessage : Message {
    override val level: String
        get() = "error"
}

interface CommonModule : Key {
    override val moduleName get() = ModuleName.COMMON
}

interface AnimeModule : Key {
    override val moduleName get() = ModuleName.ANIME
}

fun EnumKey.getEnumKey(enum: EnumKey): String {
    if (enum is Enum<*>) {
        return "enum.${enum.moduleName.name}.${enum::class.simpleName}.${enum.name}"
    } else {
        throw IllegalArgumentException("$enum is not an enum")
    }
}

fun Message.getMessageKey(enum: Message): String {
    if (enum is Enum<*>) {
        return "message.$level.${enum.moduleName.name}.${enum.name}"
    } else {
        throw IllegalArgumentException("$enum is not an enum")
    }
}