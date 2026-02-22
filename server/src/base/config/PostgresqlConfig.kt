package dev.sunriseydy.acgn.server.base.config

import dev.sunriseydy.acgn.base.interfaces.AppConfigInterface
import dev.sunriseydy.acgn.base.interfaces.CommonModule

/**
 * PostgreSQL 数据库配置
 *
 * 通过 AppConfigInterface 从配置文件/数据库中读取数据库连接参数。
 * 所有配置项共享 "config.db.postgresql" 前缀。
 *
 * @author SunriseYDY
 * @date 2026-01-19 15:27
 */
object PostgresqlConfig {
    /** 配置键前缀 */
    private const val PREFIX = "config.db.postgresql"

    object host : AppConfigInterface, CommonModule {
        override val configKey: String get() = "$PREFIX.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object port : AppConfigInterface, CommonModule {
        override val configKey: String get() = "$PREFIX.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object user : AppConfigInterface, CommonModule {
        override val configKey: String get() = "$PREFIX.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object password : AppConfigInterface, CommonModule {
        override val configKey: String get() = "$PREFIX.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object database : AppConfigInterface, CommonModule {
        override val configKey: String get() = "$PREFIX.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }
}