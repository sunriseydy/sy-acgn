package dev.sunriseydy.acgn.novel.enums

import dev.sunriseydy.acgn.base.interfaces.*

/**
 * Novel 关联类型枚举
 */
enum class NovelAssociatedType : NovelModule, AssociatedTypeInterface {
    NOVEL, NOVEL_VOLUME
}

/**
 * Novel 附加类型
 */
object NovelAdditionType {
    /** Bangumi 元数据 JSON */
    object BgmJson : NovelModule, JsonObjectAdditionTypeInterface

    /** 阅读状态 */
    object ReadingStatus : NovelModule, StringAdditionType

    /** 文件状态 */
    object FileStatus : NovelModule, StringAdditionType

    /** 封面图片 ID */
    object PosterId : NovelModule, StringAdditionType

    /** EPUB / 电子书文件路径或关联ID */
    object EpubFilePath : NovelModule, StringAdditionType
}

/**
 * 小说出版 / 连载状态
 */
enum class NovelStatusEnum : NovelModule, EnumKey {
    /** 连载中 */
    SERIALIZING,
    /** 已完结 */
    ENDED,
    /** 暂停/中断 */
    PAUSED
}

/**
 * 用户的阅读状态
 */
enum class ReadingStatusEnum : NovelModule, EnumKey {
    /** 未读 */
    UNREAD,
    /** 阅读中 */
    READING,
    /** 已读 */
    READ,
    /** 弃书 */
    DROPPED
}
