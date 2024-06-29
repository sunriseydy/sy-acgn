package dev.sunriseydy.acgn.plugins

import dev.sunriseydy.acgn.anime.db.RssItemTable
import dev.sunriseydy.acgn.anime.db.RssTable
import dev.sunriseydy.acgn.config.DatabaseKey
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureDatabases() {
    val db = connectToPostgres()
    TransactionManager.defaultDatabase = db
}

fun Application.connectToPostgres() : Database {
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
            // create tables
            SchemaUtils.createMissingTablesAndColumns(
                RssTable, RssItemTable
            )
        }
    }
}

/**
 * takes a block of code and runs it within a database transaction, through the IO Dispatcher.
 * This is designed to offload blocking jobs of work onto a thread pool
 */
suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)
