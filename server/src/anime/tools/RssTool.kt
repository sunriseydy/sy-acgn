package dev.sunriseydy.acgn.anime.tools

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.tools.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.utils.io.core.Closeable
import kotlinx.datetime.format.DateTimeComponents.Formats.RFC_1123
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

/**
 * @author SunriseYDY
 * @date 2024-07-01 14:17
 */
class RssTool : Closeable {
    private val httpClient: HttpClient = httpClient {
        Logging {
            level = LogLevel.INFO
        }
    }

    override fun close() {
        httpClient.close()
    }

    suspend fun fetchRss(url: String): Rss {
        val rss: RssXml = httpClient.get(url).body()
        return convertRss(rss, url)
    }

    private fun convertRss(rssXml: RssXml, url: String): Rss {
        val channel = rssXml.channel

        return Rss(
            id = 0u,
            link = url,
            title = channel.title,
            description = channel.description,
            ttl = channel.ttl ?: 1800,
        ).apply {
            items = channel.item.map {
                RssItem(
                    id = "",
                    rssId = 0u,
                    link = it.link,
                    guid = it.guid,
                    title = it.title,
                    description = it.description,
                    torrent = it.enclosure.first {
                        it.type == "application/x-bittorrent"
                    }.url,
                    publishedAt = RFC_1123.parse(it.pubDate).toInstantUsingOffset()
                )
            }
        }
    }

    @Serializable
    @SerialName("rss")
    class RssXml(
        @XmlElement(true)
        val channel: Channel,
    )

    @Serializable
    @SerialName("channel")
    class Channel(
        @XmlElement(true)
        val title: String,
        @XmlElement(true)
        val description: String?,
        @XmlElement(true)
        val link: String,
        @XmlElement(true)
        val ttl: Int?,
        val item: List<Item> = emptyList(),
    )

    @Serializable
    @SerialName("item")
    class Item(
        @XmlElement(true)
        val title: String,
        @XmlElement(true)
        val link: String,
        @XmlElement(true)
        val description: String?,
        @XmlElement(true)
        val pubDate: String,
        @XmlElement(true)
        val guid: String,
        val enclosure: List<Enclosure>,
    )

    @Serializable
    @SerialName("enclosure")
    class Enclosure(
        @XmlElement(false)
        val url: String,
        @XmlElement(false)
        val type: String,
    )
}