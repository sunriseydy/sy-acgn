package dev.sunriseydy.acgn.server.plugins

import dev.sunriseydy.acgn.server.anime.db.animeTables
import dev.sunriseydy.acgn.server.anime.db.rssTables
import dev.sunriseydy.acgn.server.common.db.commonModuleTables
import dev.sunriseydy.acgn.server.constants.DatabaseKey
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

fun Application.configureDatabases() {
    val db = connectToPostgres()
    TransactionManager.defaultDatabase = db
    initializeDatabase()
}

fun Application.connectToPostgres(): Database {
    val user = environment.config.property(DatabaseKey.USER).getString()
    val password = environment.config.property(DatabaseKey.PASSWORD).getString()
    val host = environment.config.property(DatabaseKey.HOST).getString()
    val port = environment.config.property(DatabaseKey.PORT).getString()
    val database = environment.config.property(DatabaseKey.DATABASE).getString()

    return Database.connect(
        url = "jdbc:postgresql://$host:$port/$database",
        user = user,
        password = password
    )
}

fun Application.initializeDatabase() {
    val database = environment.config.property(DatabaseKey.DATABASE).getString()

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