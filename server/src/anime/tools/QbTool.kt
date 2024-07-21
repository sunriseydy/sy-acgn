package dev.sunriseydy.acgn.anime.tools

import dev.sunriseydy.acgn.common.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.exception.AnimeModuleException
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.call.body
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.cookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
            throw AnimeModuleException("qb_login_failed")
        }
    }

    suspend fun invoke(block: suspend () -> HttpResponse): HttpResponse {
        if (!checkCookie()) {
            login()
        }
        val response = block()
        if (response.status.isSuccess()) {
            return response
        } else {
            if (response.status == HttpStatusCode.Forbidden) {
                login()
                return invoke(block)
            } else {
                throw AnimeModuleException("qb_request_failed")
            }
        }
    }

    suspend fun addTorrent(torrentAdd: TorrentAdd): String {
        if (torrentAdd.url.startsWith("http")) {
            TODO()
        }
        return invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_TORRENT_ADD,
                formParameters = parameters {
                    append("urls", torrentAdd.url)
                    torrentAdd.category?.let { append("category", it) }
                    append("autoTMM", torrentAdd.autoTMM.toString())
                }
            )
        }.body()
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

@Serializable
data class TorrentAdd(
    val url: String,
    val category: String?,
    val autoTMM: Boolean = true,
)