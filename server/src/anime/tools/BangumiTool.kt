package dev.sunriseydy.acgn.server.anime.tools

import dev.sunriseydy.acgn.anime.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.server.anime.tools.bangumi.model.BangumiSearchFilter
import dev.sunriseydy.acgn.server.anime.tools.bangumi.model.BangumiSearchRequest
import dev.sunriseydy.acgn.server.anime.tools.bangumi.model.BangumiSearchResponse
import dev.sunriseydy.acgn.server.anime.tools.bangumi.model.BangumiSubject
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.json.Json

/**
 * @author SunriseYDY
 * @date 2026-02-12
 */
class BangumiTool {
    private val client = HttpClientFactory.buildHttpClient {
        defaultRequest {
            url("https://api.bgm.tv/")
            userAgent(AnimeModuleAppConfig.BgmUserAgent.configValue ?: "sunriseydy/sy-acgn (https://github.com/sunriseydy/sy-acgn)")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun searchAnime(keywords: String, limit: Int = 10, offset: Int = 0): List<Anime> {
        val response: BangumiSearchResponse = client.post("v0/search/subjects") {
            parameter("limit", limit)
            parameter("offset", offset)
            setBody(
                BangumiSearchRequest(
                    keyword = keywords,
                    filter = BangumiSearchFilter(type = listOf(2)) // 2 for Anime
                )
            )
        }.body()

        return response.data.map { it.toAnime() }
    }

    suspend fun getSubject(id: Int): Anime {
        val subject: BangumiSubject = client.get("v0/subjects/$id").body()
        return subject.toAnime()
    }

    private fun BangumiSubject.toAnime(): Anime {
        val airDate = this.date?.let {
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }
    
        val bgmIdULong = this.id.toULong()
        val additions = listOf(
            AdditionalInfo(
                "", ULong.MIN_VALUE, AnimeAssociatedType.ANIME_SEASON.key,
                AnimeAdditionType.BgmJson.key, // 暂用此 Key 存储原始数据
                Json.encodeToString(BangumiSubject.serializer(), this)
            )
        )

        val season = AnimeSeason(
            id = ULong.MIN_VALUE,
            animeId = ULong.MIN_VALUE,
            name = this.nameCn.ifBlank { this.name },
            description = this.summary,
            season = 1, // Bangumi 条目通常视为独立季度，默认设为 1
            numberOfEpisodes = if (this.totalEpisodes > 0) this.totalEpisodes else this.eps,
            year = airDate?.year ?: 0,
            month = airDate?.month?.number ?: 0,
            airDate = airDate,
            bgmId = bgmIdULong,
            additions = additions
        )

        return Anime(
            id = ULong.MIN_VALUE,
            name = this.nameCn.ifBlank { this.name },
            description = this.summary,
            firstAirDate = airDate,
            bgmId = bgmIdULong,
            animeSeasons = listOf(season),
            additions = additions
        )
    }
}
