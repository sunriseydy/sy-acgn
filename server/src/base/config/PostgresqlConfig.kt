package dev.sunriseydy.acgn.server.base.config

import dev.sunriseydy.acgn.base.interfaces.AppConfigInterface
import dev.sunriseydy.acgn.base.interfaces.CommonModule

/**
 * @author SunriseYDY
 * @date 2026-01-19 15:27
 */
object PostgresqlConfig {
    object host : AppConfigInterface, CommonModule {
        override val configKey: String get() = "config.db.postgresql.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object port : AppConfigInterface, CommonModule {
        override val configKey: String get() = "config.db.postgresql.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object user : AppConfigInterface, CommonModule {
        override val configKey: String get() = "config.db.postgresql.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object password : AppConfigInterface, CommonModule {
        override val configKey: String get() = "config.db.postgresql.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

    object database : AppConfigInterface, CommonModule {
        override val configKey: String get() = "config.db.postgresql.${this::class.simpleName}"
        override val configValue: String get() = this.stringValue!!
    }

}