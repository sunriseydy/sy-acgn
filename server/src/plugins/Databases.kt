package dev.sunriseydy.acgn.plugins

import dev.sunriseydy.acgn.config.DatabaseKey
import dev.sunriseydy.acgn.db.anime.*
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    connectToPostgres()
}

fun Application.connectToPostgres() {
    val user = environment.config.property(DatabaseKey.USER).getString()
    val password = environment.config.property(DatabaseKey.PASSWORD).getString()
    val host = environment.config.property(DatabaseKey.HOST).getString()
    val port = environment.config.property(DatabaseKey.PORT).getString()
    val database = environment.config.property(DatabaseKey.DATABASE).getString()

    Database.connect(
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
        addLogger(StdOutSqlLogger)
        // create tables
        SchemaUtils.createMissingTablesAndColumns(
            RssTable, RssItemTable
        )
    }
}

/**
 * takes a block of code and runs it within a database transaction, through the IO Dispatcher.
 * This is designed to offload blocking jobs of work onto a thread pool
 */
suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)
