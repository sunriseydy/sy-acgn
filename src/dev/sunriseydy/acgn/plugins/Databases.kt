package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.*
import java.sql.Connection
import java.sql.DriverManager

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres()
}

fun Application.connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")
    val user = environment.config.property("acgn.postgres.user").getString()
    val password = environment.config.property("acgn.postgres.password").getString()
    val host = environment.config.property("acgn.postgres.host").getString()
    val port = environment.config.property("acgn.postgres.port").getString()
    val database = environment.config.property("acgn.postgres.database").getString()

    return DriverManager.getConnection("jdbc:postgresql://$host:$port/$database", user, password)
}
