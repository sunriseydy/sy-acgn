package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.anime.db.animeTables
import dev.sunriseydy.acgn.server.anime.db.rssTables
import dev.sunriseydy.acgn.server.base.config.PostgresqlConfig
import dev.sunriseydy.acgn.server.common.db.commonModuleTables
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

fun Application.configureDatabases() {
    val db = connectToPostgresql()
    TransactionManager.defaultDatabase = db
    initializeDatabase(db)
}

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
                rssTables(),
                animeTables(),
                commonModuleTables()
            ).flatten().toTypedArray())
        ).also {
            environment.log.info("database migration: $it")
            if (it.isNotEmpty()) {
                this.execInBatch(it)
            }
        }
    }
}

fun <T> SizedIterable<T>.paging(page: Long? = null, size: Int? = null) =
    if (page == null || size == null || page <= 0) {
        this
    } else {
        this.limit(size).offset((page - 1) * size)
    }