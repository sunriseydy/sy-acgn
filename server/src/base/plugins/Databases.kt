package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.anime.db.animeTables
import dev.sunriseydy.acgn.server.base.config.PostgresqlConfig
import dev.sunriseydy.acgn.server.common.db.commonModuleTables
import dev.sunriseydy.acgn.server.novel.db.novelTables
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

/**
 * 配置数据库
 *
 * 初始化数据库连接并设置 Exposed 的默认数据库。
 * 执行数据库初始化操作（建表/迁移）。
 */
fun Application.configureDatabases() {
    val db = connectToPostgresql()
    TransactionManager.defaultDatabase = db
    initializeDatabase(db)
}

/**
 * 连接到 PostgreSQL 数据库
 *
 * 从配置中读取数据库连接参数（主机、端口、用户名、密码、数据库名）。
 */
fun connectToPostgresql(): Database {
    val user = PostgresqlConfig.user.configValue
    val password = PostgresqlConfig.password.configValue
    val host = PostgresqlConfig.host.configValue
    val port = PostgresqlConfig.port.configValue
    val database = PostgresqlConfig.database.configValue

    return Database.connect(
        url = "jdbc:postgresql://$host:$port/$database",
        user = user,
        password = password
    )
}

/**
 * 初始化数据库
 *
 * 在事务中执行：
 * 1. 检查并创建数据库（若不存在）。
 * 2. 对所有注册的表（Anime 模块、Common 模块）执行自动迁移，确保表结构与代码一致。
 */
fun Application.initializeDatabase(db: Database) {
    val database = PostgresqlConfig.database.configValue

    transaction {
        // create database if not exists
        SchemaUtils.listDatabases().firstOrNull { it == database } ?: run {
            SchemaUtils.createDatabase(database)
        }
        // migrate tables
        MigrationUtils.statementsRequiredForDatabaseMigration(
            *(listOf(
                animeTables(),
                commonModuleTables(),
                novelTables()
            ).flatten().toTypedArray())
        ).also {
            environment.log.info("database migration: $it")
            if (it.isNotEmpty()) {
                this.execInBatch(it)
            }
        }
    }
}

/**
 * Exposed 分页扩展函数
 *
 * 为 SizedIterable 添加分页功能。
 * @param page 页码（从 1 开始）
 * @param size 每页大小
 */
fun <T> SizedIterable<T>.paging(page: Long? = null, size: Int? = null) =
    if (page == null || size == null || page <= 0) {
        this
    } else {
        this.limit(size).offset((page - 1) * size)
    }