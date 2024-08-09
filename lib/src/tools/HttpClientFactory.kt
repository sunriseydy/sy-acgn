package dev.sunriseydy.acgn.tools

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig

object HttpClientFactory {
    @OptIn(ExperimentalXmlUtilApi::class, ExperimentalSerializationApi::class)
    val httpClientConfig: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                encodeDefaults = true
            })
            xml(format = XML {
                defaultPolicy {
                    unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                }
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }

    fun buildHttpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient = HttpClient {
        httpClientConfig(this)
        config(this)
    }
}