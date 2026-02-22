package dev.sunriseydy.acgn.common.dto

import dev.sunriseydy.acgn.base.serializer.KotlinVersionSerializer
import kotlinx.serialization.Serializable

/**
 * 应用信息 DTO
 *
 * 封装应用的版本信息、配置和本地化数据，用于客户端初始化。
 *
 * @property version 应用版本号
 * @property configs 配置映射 (key -> Pair(数据库配置, 文件配置))
 * @property localizations 本地化字符串映射
 *
 * @author SunriseYDY
 * @date 2024-08-08 23:33
 */
@Serializable
data class AppInfo(
    @Serializable(KotlinVersionSerializer::class) val version: KotlinVersion = KotlinVersion(0, 0, 1),
    val configs: Map<String, Pair<AppConfig?, String?>> = emptyMap(),
    val localizations: Map<String, String> = emptyMap(),
)
