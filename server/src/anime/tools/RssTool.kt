package dev.sunriseydy.acgn.anime.tools

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.client.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

/**
 * @author SunriseYDY
 * @date 2024-07-01 14:17
 */
class RssTool: Closeable {
    private val httpClient: HttpClient = httpClient()

    override fun close() {
        httpClient.close()
    }

    suspend fun fetchRss(url: String): Rss {
        val rss: RssXml = httpClient.get(url).body()
        return convertRss(rss)
    }

    fun convertRss(rssXml: RssXml): Rss {
        val channel = rssXml.channel

        return Rss().apply {
            link = channel.link
            title = channel.title
            description = channel.description
            ttl = channel.ttl
            items = channel.item?.map {
                RssItem().apply {
                    title = it.title
                    link = it.link
                    description = it.description
                    guid = it.guid
                    torrent = it.enclosure.first {
                        it.type == "application/x-bittorrent"
                    }.url
                }
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
        val item: List<Item>?,
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
        val pubDate: String?,
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