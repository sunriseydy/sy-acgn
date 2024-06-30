package dev.sunriseydy.acgn.client

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml

fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) {
        json()
        xml()
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
    config(this)
}