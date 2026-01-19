package dev.sunriseydy.acgn.server.anime.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.server.anime.service.RssService
import dev.sunriseydy.acgn.server.anime.service.RssServiceImpl
import dev.sunriseydy.acgn.server.anime.tools.QbTool
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*

fun Route.rssRoutes(rssService: RssService = RssServiceImpl()) {
    route("/rss") {
        get {
            call.respond(Result(data = rssService.getAllRss()))
        }
        put("/{id}") {
            call.respond(
                Result(
                    data = rssService.saveRss(
                        call.receive<Rss>().copy(id = call.parameters["id"]!!.toULong())
                    )
                )
            )
        }
        put("/fetch") {
            call.respond(Result(data = rssService.fetchRss(call.parameters["rssId"]?.toULong())))
        }
        post {
            call.receive(JsonObject::class).getValue("link").also {
                call.respond(Result(data = rssService.createRss(it.jsonPrimitive.content)))
            }
        }
        delete("/{id}") {
            call.respond(Result(data = rssService.removeRss(call.parameters["id"]!!.toULong())))
        }

        route("/item") {
            get {
                call.respond(
                    Result(
                        data = rssService.getRssItemByRssIdOrIsRead(
                            rssId = call.parameters["rssId"]?.toULong(),
                            isRead = call.parameters["isRead"]?.toBoolean(),
                            page = call.parameters["page"]?.toLong(),
                            size = call.parameters["size"]?.toInt(),
                        )
                    )
                )
            }
            put("/read") {
                call.respond(
                    Result(
                        data = rssService.markRssItemReadByIdOrRssId(
                            id = call.parameters["id"]?.let { UUID.fromString(it) },
                            rssId = call.parameters["rssId"]?.toULong(),
                        )
                    )
                )
            }
        }
    }

    route("/qb/torrent") {
        get("/{hash}") {
            call.respond(Result(data = QbTool().getTorrentInfo(call.parameters["hash"]!!)))
        }
        post {
            call.respond(Result(data = QbTool().addTorrent(call.receive())))
        }
    }
}