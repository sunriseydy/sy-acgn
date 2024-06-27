package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureDatabases() {
    connectToPostgres()
}

fun Application.connectToPostgres() {
    Class.forName("org.postgresql.Driver")
    val user = environment.config.property("acgn.postgres.user").getString()
    val password = environment.config.property("acgn.postgres.password").getString()
    val host = environment.config.property("acgn.postgres.host").getString()
    val port = environment.config.property("acgn.postgres.port").getString()
    val database = environment.config.property("acgn.postgres.database").getString()

    Database.connect(
        url = "jdbc:postgresql://$host:$port/$database",
        user = user,
        password = password
    )
}

/**
 * takes a block of code and runs it within a database transaction, through the IO Dispatcher.
 * This is designed to offload blocking jobs of work onto a thread pool
 */
suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)
