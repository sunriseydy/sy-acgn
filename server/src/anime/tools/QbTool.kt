package dev.sunriseydy.acgn.server.anime.tools

import anime.tools.torrent.TorrentParser
import dev.sunriseydy.acgn.anime.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.anime.dto.TorrentAdd
import dev.sunriseydy.acgn.anime.enums.AnimeModuleError
import dev.sunriseydy.acgn.base.exception.MessageException
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.ByteArrayInputStream
import java.net.URLDecoder

/**
 * @author SunriseYDY
 * @date 2024-07-20 16:03
 */
class QbTool {

    val QB_COOKIE_NAME = "SID"

    val userName = AnimeModuleAppConfig.QbUserName.configValue
    val password = AnimeModuleAppConfig.QbPassword.configValue
    val apiBaseUrl = AnimeModuleAppConfig.QbApiBaseUrl.configValue

    init {
        checkNotNull(apiBaseUrl) { "Qb api base url is null" }
    }

    private val httpClient = HttpClientFactory.buildHttpClient {
        install(HttpCookies)
        expectSuccess = false
        Logging {
            level = LogLevel.INFO
        }
    }

    suspend fun checkCookie() =
        httpClient.cookies(apiBaseUrl!!)
            .any { it.name == QB_COOKIE_NAME }

    suspend fun login() {
        httpClient.submitForm(
            url = apiBaseUrl + QbUrl.QB_LOGIN,
            formParameters = parameters {
                userName?.let { append("username", it) }
                password?.let { append("password", it) }
            }
        )
        if (!checkCookie()) {
            throw MessageException(AnimeModuleError.QB_LOGIN_FAILED)
        }
    }

    suspend fun invoke(block: suspend () -> HttpResponse): HttpResponse {
        if (!checkCookie()) {
            login()
        }
        val response = block()
        if (response.status.isSuccess()) {
            println(response.bodyAsText())
            return response
        } else {
            if (response.status == HttpStatusCode.Forbidden) {
                login()
                return invoke(block)
            } else {
                throw MessageException(AnimeModuleError.QB_REQUEST_FAILED)
            }
        }
    }

    suspend fun addTorrent(torrentAdd: TorrentAdd): String {
        val hash: String
        var bytes: ByteArray? = null
        if (torrentAdd.url.startsWith("http")) {
            val response = httpClient.get(torrentAdd.url)
            if (!response.status.isSuccess()) {
                throw MessageException(AnimeModuleError.QB_DOWNLOAD_TORRENT_FAILED)
            }
            bytes = response.body()
            val torrent = TorrentParser.parseTorrent(ByteArrayInputStream(bytes))
            if (torrent == null) {
                throw MessageException(AnimeModuleError.QB_PARSE_TORRENT_FAILED)
            } else {
                hash = torrent.info_hash
            }
        } else if (torrentAdd.url.startsWith("magnet:")) {
            hash =
                this.extractInfoHash(torrentAdd.url) ?: throw MessageException(AnimeModuleError.QB_PARSE_MAGNET_FAILED)
        } else {
            throw MessageException(AnimeModuleError.QB_PARSE_HASH_FAILED)
        }
        invoke {
            httpClient.submitFormWithBinaryData(
                url = apiBaseUrl + QbUrl.QB_TORRENT_ADD,
                formData = formData {
                    bytes?.let { append("torrents", it) } ?: append("urls", torrentAdd.url)
                    TODO("默认分类")
                    torrentAdd.category?.let { append("category", it) }
                    append("autoTMM", torrentAdd.autoTMM.toString())
                }
            )
        }
        return hash.lowercase()
    }

    suspend fun getTorrentInfo(hash: String): TorrentInfo =
        invoke {
            httpClient.get(urlString = apiBaseUrl!!) {
                url {
                    appendPathSegments(QbUrl.QB_TORRENT_DETAIL)
                    parameters.append("hash", hash)
                }
            }
        }.body()

    fun extractInfoHash(magnetLink: String): String? {
        val params = magnetLink.substringAfter("magnet:?").split("&")
        for (param in params) {
            val keyValue = param.split("=")
            if (keyValue.size == 2 && keyValue[0] == "xt" && keyValue[1].startsWith("urn:btih:")) {
                return URLDecoder.decode(keyValue[1].substringAfter("urn:btih:"), "UTF-8")
            }
        }
        return null
    }
}

object QbUrl {
    const val QB_LOGIN = "/api/v2/auth/login"
    const val QB_TORRENT_ADD = "/api/v2/torrents/add"
    const val QB_TORRENT_DETAIL = "/api/v2/torrents/properties"
}

@Serializable
data class TorrentInfo(
    @SerialName("completion_date") val completionDate: Long,
    @SerialName("download_path") val downloadPath: String,
    @SerialName("eta") val eta: Long,
    @SerialName("hash") val hash: String,
    @SerialName("name") val name: String,
    @SerialName("save_path") val savePath: String,
)