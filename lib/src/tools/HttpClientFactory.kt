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

/**
 * HTTP 客户端工厂
 *
 * 提供统一配置的 Ktor HTTP 客户端创建方式。
 * 预配置了 JSON/XML 内容协商和日志功能。
 *
 * @author SunriseYDY
 * @date 2024-07-12
 */
object HttpClientFactory {

    /**
     * JSON 序列化配置
     *
     * - [ignoreUnknownKeys] 忽略未知字段，提升兼容性
     * - [isLenient] 宽松解析模式
     * - [explicitNulls] 不序列化 null 字段
     * - [encodeDefaults] 序列化默认值
     */
    @OptIn(ExperimentalSerializationApi::class)
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }

    /**
     * 默认的 HTTP 客户端配置
     *
     * @param logLevel 日志级别，默认为 [LogLevel.INFO]
     */
    @OptIn(ExperimentalXmlUtilApi::class)
    fun httpClientConfig(logLevel: LogLevel = LogLevel.INFO): HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(ContentNegotiation) {
            json(jsonConfig)
            xml(format = XML {
                defaultPolicy {
                    unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                }
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = logLevel
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }

    /**
     * 构建 HTTP 客户端
     *
     * @param logLevel 日志级别，默认为 [LogLevel.INFO]
     * @param config 额外的自定义配置
     * @return 配置好的 [HttpClient] 实例
     */
    fun buildHttpClient(
        logLevel: LogLevel = LogLevel.INFO,
        config: HttpClientConfig<*>.() -> Unit = {}
    ): HttpClient = HttpClient {
        httpClientConfig(logLevel)(this)
        config(this)
    }
}