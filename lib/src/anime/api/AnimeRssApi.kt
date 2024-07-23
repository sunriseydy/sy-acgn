package dev.sunriseydy.acgn.anime.api

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.animeApiEndPoint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get

/**
 * @author SunriseYDY
 * @date 2024-07-23 11:33
 */
class AnimeRssApi internal constructor(private val httpClient: HttpClient) {
    suspend fun getAllRss(): List<Rss> = httpClient.get {
        animeRssApiEndPoint()
    }.body()

    private fun HttpRequestBuilder.animeRssApiEndPoint(vararg paths: String) {
        animeApiEndPoint("rss", *paths)
    }
}