package dev.sunriseydy.acgn.server.anime.tools

import dev.sunriseydy.acgn.anime.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.enums.AnimeAdditionType
import dev.sunriseydy.acgn.anime.enums.AnimeAssociatedType
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.server.anime.tools.bangumi.model.BangumiRelatedSubject
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

    suspend fun searchAnime(keywords: String, limit: Int = 10, offset: Int = 0): List<AnimeSeason> {
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

        return response.data.map { it.toAnimeSeason() }
    }

    suspend fun getSubject(id: Int): AnimeSeason {
        val subject: BangumiSubject = client.get("v0/subjects/$id").body()
        return subject.toAnimeSeason()
    }

    private fun BangumiSubject.toAnimeSeason(): AnimeSeason {
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
        return AnimeSeason(
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
    }

    private fun BangumiSubject.toAnime(): Anime {
        val season = toAnimeSeason()

        return Anime(
            id = ULong.MIN_VALUE,
            name = this.nameCn.ifBlank { this.name },
            description = this.summary,
            firstAirDate = season.airDate,
            bgmId = season.bgmId,
            animeSeasons = listOf(season)
        )
    }

    suspend fun searchNovel(keywords: String, limit: Int = 10, offset: Int = 0): List<dev.sunriseydy.acgn.novel.dto.Novel> {
        val response: BangumiSearchResponse = client.post("v0/search/subjects") {
            parameter("limit", limit)
            parameter("offset", offset)
            setBody(
                BangumiSearchRequest(
                    keyword = keywords,
                    filter = BangumiSearchFilter(type = listOf(1)) // 1 for Book/Novel
                )
            )
        }.body()

        return response.data.map { it.toNovel() }
    }

    suspend fun getNovelSubject(id: Int): dev.sunriseydy.acgn.novel.dto.Novel {
        val subject: BangumiSubject = client.get("v0/subjects/$id").body()
        return subject.toNovel()
    }

    suspend fun getNovelVolumes(bgmId: Int, novelId: ULong = ULong.MIN_VALUE): List<dev.sunriseydy.acgn.novel.dto.NovelVolume> {
        val relatedSubjects: List<BangumiRelatedSubject> = try {
            client.get("v0/subjects/$bgmId/subjects").body()
        } catch (e: Exception) {
            emptyList()
        }
        val volumeRelations = relatedSubjects.filter { it.relation == "单行本" }
        return volumeRelations.mapIndexedNotNull { index, related ->
            val subject: BangumiSubject = try {
                client.get("v0/subjects/${related.id}").body()
            } catch (e: Exception) {
                null
            } ?: return@mapIndexedNotNull null

            val title = subject.nameCn.ifBlank { subject.name }
            val volNum = parseVolumeNumber(title) ?: (index + 1).toDouble()
            subject.toNovelVolume(novelId = novelId, volumeNumber = volNum)
        }
    }

    private fun BangumiSubject.toNovel(): dev.sunriseydy.acgn.novel.dto.Novel {
        val bgmIdULong = this.id.toULong()
        var authorStr: String? = null
        var illustratorStr: String? = null
        var publisherStr: String? = null

        infobox?.forEach { wiki ->
            when (wiki.key) {
                "作者" -> authorStr = wiki.value.toString().removeSurrounding("\"")
                "插画", "插图" -> illustratorStr = wiki.value.toString().removeSurrounding("\"")
                "出版社" -> publisherStr = wiki.value.toString().removeSurrounding("\"")
            }
        }

        val additions = listOf(
            AdditionalInfo(
                "", ULong.MIN_VALUE, dev.sunriseydy.acgn.novel.enums.NovelAssociatedType.NOVEL.key,
                dev.sunriseydy.acgn.novel.enums.NovelAdditionType.BgmJson.key,
                Json.encodeToString(BangumiSubject.serializer(), this)
            )
        )
        return dev.sunriseydy.acgn.novel.dto.Novel(
            id = ULong.MIN_VALUE,
            name = this.nameCn.ifBlank { this.name },
            originalName = this.name,
            author = authorStr,
            illustrator = illustratorStr,
            description = this.summary.replace(Regex("(\\r?\\n){3,}"), "\n\n").trim(),
            publisher = publisherStr,
            status = dev.sunriseydy.acgn.novel.enums.NovelStatusEnum.SERIALIZING.name,
            totalVolumes = this.volumes,
            bgmId = bgmIdULong,
            additions = additions
        )
    }

    private fun BangumiSubject.toNovelVolume(novelId: ULong, volumeNumber: Double): dev.sunriseydy.acgn.novel.dto.NovelVolume {
        val bgmIdULong = this.id.toULong()
        var isbnStr: String? = null

        infobox?.forEach { wiki ->
            when (wiki.key) {
                "ISBN" -> isbnStr = wiki.value.toString().removeSurrounding("\"")
            }
        }

        val releaseDate = this.date?.let {
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }

        val additions = listOf(
            AdditionalInfo(
                "", ULong.MIN_VALUE, dev.sunriseydy.acgn.novel.enums.NovelAssociatedType.NOVEL_VOLUME.key,
                dev.sunriseydy.acgn.novel.enums.NovelAdditionType.BgmJson.key,
                Json.encodeToString(BangumiSubject.serializer(), this)
            )
        )
        return dev.sunriseydy.acgn.novel.dto.NovelVolume(
            id = ULong.MIN_VALUE,
            novelId = novelId,
            volumeNumber = volumeNumber,
            name = this.nameCn.ifBlank { this.name },
            description = this.summary.replace(Regex("(\\r?\\n){3,}"), "\n\n").trim(),
            releaseDate = releaseDate,
            isbn = isbnStr,
            bgmId = bgmIdULong,
            additions = additions
        )
    }

    private fun parseVolumeNumber(name: String): Double? {
        val regexes = listOf(
            Regex("""[第卷]\s*(\d+(?:\.\d+)?)\s*[卷册]?"""),
            Regex("""(?i)vol(?:ume)?\.?\s*(\d+(?:\.\d+)?)"""),
            Regex("""[（\(](\d+(?:\.\d+)?)[）\)]""")
        )
        for (regex in regexes) {
            val match = regex.find(name)
            if (match != null) {
                val numStr = match.groupValues.getOrNull(1)
                if (!numStr.isNullOrBlank()) {
                    return numStr.toDoubleOrNull()
                }
            }
        }
        return null
    }

    suspend fun searchGame(keywords: String, limit: Int = 10, offset: Int = 0): List<dev.sunriseydy.acgn.game.dto.Game> {
        val response: BangumiSearchResponse = client.post("v0/search/subjects") {
            parameter("limit", limit)
            parameter("offset", offset)
            setBody(
                BangumiSearchRequest(
                    keyword = keywords,
                    filter = BangumiSearchFilter(type = listOf(4)) // 4 for Game
                )
            )
        }.body()

        return response.data.map { it.toGame() }
    }

    suspend fun getGameSubject(id: Int): dev.sunriseydy.acgn.game.dto.Game {
        val subject: BangumiSubject = client.get("v0/subjects/$id").body()
        return subject.toGame()
    }

    private fun BangumiSubject.toGame(): dev.sunriseydy.acgn.game.dto.Game {
        val bgmIdULong = this.id.toULong()
        var devStr: String? = null
        var pubStr: String? = null

        infobox?.forEach { wiki ->
            when (wiki.key) {
                "开发", "制作" -> devStr = wiki.value.toString().removeSurrounding("\"")
                "发行" -> pubStr = wiki.value.toString().removeSurrounding("\"")
            }
        }

        val releaseDate = this.date?.let {
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }

        val additions = listOf(
            AdditionalInfo(
                "", ULong.MIN_VALUE, dev.sunriseydy.acgn.game.enums.GameAssociatedType.GAME.key,
                dev.sunriseydy.acgn.game.enums.GameAdditionType.BgmJson.key,
                Json.encodeToString(BangumiSubject.serializer(), this)
            )
        )
        return dev.sunriseydy.acgn.game.dto.Game(
            id = ULong.MIN_VALUE,
            name = this.nameCn.ifBlank { this.name },
            originalName = this.name,
            developer = devStr,
            publisher = pubStr,
            description = this.summary.replace(Regex("(\\r?\\n){3,}"), "\n\n").trim(),
            releaseDate = releaseDate,
            bgmId = bgmIdULong,
            rating = this.rating?.score?.takeIf { it > 0.0 },
            additions = additions
        )
    }
}

