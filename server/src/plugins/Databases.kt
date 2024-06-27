package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

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
