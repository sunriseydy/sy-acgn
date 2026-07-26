package dev.sunriseydy.acgn.novel.enums

import dev.sunriseydy.acgn.base.interfaces.ErrorMessage
import dev.sunriseydy.acgn.base.interfaces.NovelModule

/**
 * 轻小说模块错误信息枚举
 */
enum class NovelModuleError : NovelModule, ErrorMessage {
    NOVEL_NOT_FOUND,
    NOVEL_VOLUME_NOT_FOUND,
    BANGUMI_IMPORT_FAILED,
    INVALID_VOLUME_NUMBER,
}
