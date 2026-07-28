package dev.sunriseydy.acgn.game.enums

import dev.sunriseydy.acgn.base.interfaces.*

/**
 * Game 关联类型枚举
 */
enum class GameAssociatedType : GameModule, AssociatedTypeInterface {
    GAME, GAME_RELEASE, GAME_PLAY_RECORD
}

/**
 * Game 附加类型
 */
object GameAdditionType {
    /** Bangumi 元数据 JSON */
    object BgmJson : GameModule, JsonObjectAdditionTypeInterface

    /** Steam 元数据 JSON */
    object SteamJson : GameModule, JsonObjectAdditionTypeInterface

    /** 游玩状态 */
    object PlayStatus : GameModule, StringAdditionType

    /** 封面图片 ID */
    object PosterId : GameModule, StringAdditionType
}

/**
 * 玩家的游玩状态
 */
enum class GamePlayStatusEnum : GameModule, EnumKey {
    /** 未开始 */
    UNPLAYED,
    /** 游玩中 */
    PLAYING,
    /** 已通关 */
    COMPLETED,
    /** 全成就 / 完美全收集 */
    MASTERED,
    /** 搁置 / 弃坑 */
    DROPPED
}

/**
 * 游戏平台/发售渠道枚举
 */
enum class GamePlatformEnum : GameModule, EnumKey {
    // PC 数字商城 & 渠道
    STEAM,
    EPIC,
    GOG,
    MICROSOFT_STORE,
    DLSITE,
    OTHER_PC,

    // 主机 & 掌机
    SWITCH,
    PS5,
    PS4,
    PS3,
    XBOX,

    // 移动端及其他
    ANDROID,
    IOS,
    OTHERS
}
