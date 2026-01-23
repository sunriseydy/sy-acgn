package dev.sunriseydy.acgn.server.anime.routes

import dev.sunriseydy.acgn.anime.AnimeModuleResource
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.server.anime.service.RssService
import dev.sunriseydy.acgn.server.anime.tools.QbTool
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*

fun Route.rssRoutes() {
    val rssService: RssService by application.dependencies
    get<AnimeModuleResource.Rss> {
        call.respond(Result(data = rssService.getAllRss()))
    }
    put<AnimeModuleResource.Rss.Id> { resource ->
        call.respond(
            Result(
                data = rssService.saveRss(
                    call.receive<Rss>().copy(id = resource.id)
                )
            )
        )
    }
    put<AnimeModuleResource.Rss.Fetch> { resource ->
        call.respond(Result(data = rssService.fetchRss(resource.rssId)))
    }
    post<AnimeModuleResource.Rss> {
        call.receive(JsonObject::class).getValue("link").also {
            call.respond(Result(data = rssService.createRss(it.jsonPrimitive.content)))
        }
    }
    delete<AnimeModuleResource.Rss.Id> { resource ->
        call.respond(Result(data = rssService.removeRss(resource.id)))
    }

    get<AnimeModuleResource.Rss.Item> { resource ->
        call.respond(
            Result(
                data = rssService.getRssItemByRssIdOrIsRead(
                    rssId = resource.rssId,
                    isRead = resource.isRead,
                    page = resource.page,
                    size = resource.size,
                )
            )
        )
    }
    put<AnimeModuleResource.Rss.Item.Read> { resource ->
        call.respond(
            Result(
                data = rssService.markRssItemReadByIdOrRssId(
                    id = resource.id?.let { UUID.fromString(it) },
                    rssId = resource.rssId,
                )
            )
        )
    }

    get<AnimeModuleResource.Qb.Torrent.Hash> { resource ->
        call.respond(Result(data = QbTool().getTorrentInfo(resource.hash)))
    }
    post<AnimeModuleResource.Qb.Torrent> {
        call.respond(Result(data = QbTool().addTorrent(call.receive())))
    }
}