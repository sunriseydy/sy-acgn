package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.*
import java.sql.Connection
import java.sql.DriverManager

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres()
}

fun Application.connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")
    val url = environment.config.property("acgn.postgres.url").getString()
    val user = environment.config.property("acgn.postgres.user").getString()
    val password = environment.config.property("acgn.postgres.password").getString()

    return DriverManager.getConnection(url, user, password)
}
