package dev.sunriseydy.acgn.game.enums

import dev.sunriseydy.acgn.base.interfaces.ErrorMessage
import dev.sunriseydy.acgn.base.interfaces.GameModule

/**
 * 游戏模块错误信息枚举
 */
enum class GameModuleError : GameModule, ErrorMessage {
    GAME_NOT_FOUND,
    GAME_RELEASE_NOT_FOUND,
    BANGUMI_IMPORT_FAILED,
    STEAM_IMPORT_FAILED,
}
