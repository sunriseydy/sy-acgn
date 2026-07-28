package dev.sunriseydy.acgn.game.dto

import dev.sunriseydy.acgn.base.interfaces.AdditionInterface
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.game.enums.GameAdditionType
import dev.sunriseydy.acgn.game.enums.GamePlatformEnum
import dev.sunriseydy.acgn.game.enums.GamePlayStatusEnum
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * 游戏实体 DTO
 *
 * @property id 游戏 ID
 * @property name 游戏名称（中文/通用名）
 * @property originalName 原名/外文名
 * @property developer 开发商/社团
 * @property publisher 发行商
 * @property description 简介
 * @property releaseDate 首发日期
 * @property bgmId Bangumi 关联 ID
 * @property steamId Steam AppID
 * @property rating 综合评分
 * @property releases 关联的各平台发行版本列表
 * @property playRecord 游玩记录
 * @property additions 附加信息列表
 */
@Serializable
data class Game(
    val id: ULong,
    val name: String,
    val originalName: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val description: String? = null,
    val releaseDate: LocalDate? = null,
    val bgmId: ULong? = null,
    val steamId: ULong? = null,
    val rating: Double? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val releases: List<GameRelease> = emptyList(),
    val playRecord: GamePlayRecord? = null,
    override val additions: List<AdditionalInfo> = emptyList(),
) : AdditionInterface {
    val bgmJson: JsonObject? get() = GameAdditionType.BgmJson.valueOf(additions)
    val steamJson: JsonObject? get() = GameAdditionType.SteamJson.valueOf(additions)
    val posterId: String? get() = GameAdditionType.PosterId.valueOf(additions)
}

/**
 * 游戏平台发行版本 DTO
 *
 * @property id 发行 ID
 * @property gameId 关联的游戏 ID
 * @property platform 发行平台 [GamePlatformEnum]
 * @property releaseDate 该平台发行日期
 * @property version 版本号/DLC/版本名称
 * @property language 语言支持信息
 */
@Serializable
data class GameRelease(
    val id: ULong,
    val gameId: ULong,
    val platform: String = GamePlatformEnum.STEAM.name,
    val releaseDate: LocalDate? = null,
    val version: String? = null,
    val language: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

/**
 * 玩家游玩记录 DTO
 *
 * @property id 记录 ID
 * @property gameId 关联的游戏 ID
 * @property playStatus 游玩状态 [GamePlayStatusEnum]
 * @property playTimeMinutes 累计游玩时长（分钟）
 * @property clearCount 通关周目/次数
 * @property score 个人评分 (0-10)
 * @property comment 评价/心得
 * @property lastPlayedAt 最近游玩时间
 */
@Serializable
data class GamePlayRecord(
    val id: ULong,
    val gameId: ULong,
    val playStatus: String = GamePlayStatusEnum.UNPLAYED.name,
    val playTimeMinutes: Long = 0,
    val clearCount: Int = 0,
    val score: Double? = null,
    val comment: String? = null,
    val lastPlayedAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

/**
 * 创建/更新游戏主实体请求体
 */
@Serializable
data class GameCreateOrUpdateDto(
    val id: ULong? = null,
    val name: String,
    val originalName: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val description: String? = null,
    val releaseDate: LocalDate? = null,
    val bgmId: ULong? = null,
    val steamId: ULong? = null,
    val rating: Double? = null,
)

/**
 * 创建/更新游戏发行平台请求体
 */
@Serializable
data class GameReleaseCreateOrUpdateDto(
    val id: ULong? = null,
    val gameId: ULong,
    val platform: String = GamePlatformEnum.STEAM.name,
    val releaseDate: LocalDate? = null,
    val version: String? = null,
    val language: String? = null,
)

/**
 * 创建/更新游玩记录请求体
 */
@Serializable
data class GamePlayRecordCreateOrUpdateDto(
    val id: ULong? = null,
    val gameId: ULong,
    val playStatus: String = GamePlayStatusEnum.UNPLAYED.name,
    val playTimeMinutes: Long = 0,
    val clearCount: Int = 0,
    val score: Double? = null,
    val comment: String? = null,
    val lastPlayedAt: Instant? = null,
)
