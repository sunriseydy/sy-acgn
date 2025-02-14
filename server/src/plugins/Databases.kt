package dev.sunriseydy.acgn.server.plugins

import MigrationUtils
import dev.sunriseydy.acgn.server.anime.db.animeTables
import dev.sunriseydy.acgn.server.anime.db.rssTables
import dev.sunriseydy.acgn.server.common.db.commonModuleTables
import dev.sunriseydy.acgn.server.constants.DatabaseKey
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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

    runBlocking {
        suspendTransaction {
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
                ).flatMap { it }.toTypedArray())
            ).also {
                environment.log.info("database migration: $it")
                if (it.isNotEmpty()) {
                    execInBatch(it)
                }
            }
        }
    }
}

/**
 * takes a block of code and runs it within a database transaction, through the IO Dispatcher.
 * This is designed to offload blocking jobs of work onto a thread pool
 */
suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun <T> SizedIterable<T>.paging(page: Long? = null, size: Int? = null) =
    if (page == null || size == null || page <= 0) {
        this
    } else {
        this.limit(size).offset((page - 1) * size)
    }