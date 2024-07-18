package dev.sunriseydy.acgn.anime.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.service.RssService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

fun Routing.rssRoutes(rssService: RssService = RssService()) {
    route("/rss") {
        get {
            call.respond(Result(data = rssService.getAllRss()))
        }
        put("/{id}") {
            call.respond(Result(data = rssService.saveRss(call.receive<Rss>().copy(id = call.parameters["id"]!!.toULong()))))
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
                val page = call.parameters["page"]?.toLong() ?: 0
                val size = call.parameters["size"]?.toInt() ?: 10
                call.respond(
                    Result(data = rssService.getRssItemByRssIdOrIsRead(
                        rssId = call.parameters["rssId"]?.toULong(),
                        isRead = call.parameters["isRead"]?.toBoolean(),
                        page = page,
                        size = size,
                    ))
                )
            }
            put("/read") {
                call.respond(
                    Result(data = rssService.markRssItemReadByIdOrRssId(
                        id = call.parameters["id"]?.let { UUID.fromString(it) },
                        rssId = call.parameters["rssId"]?.toULong(),
                    ))
                )
            }
        }
    }
}