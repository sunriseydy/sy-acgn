package dev.sunriseydy.acgn.anime.routes

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.service.RssService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.rssRoutes(rssService: RssService = RssService()) {
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
            call.respond(Result(data = rssService.createRss(call.receive<Rss>().link)))
        }
        delete("/{id}") {
            call.respond(Result(data = rssService.removeRss(call.parameters["id"]!!.toULong())))
        }
        route("/item") {
            get {
                call.respond(
                    Result(data = rssService.getRssItemByRssIdOrIsRead(
                        rssId = call.parameters["rssId"]?.toULong(),
                        isRead = call.parameters["isRead"]?.toBoolean(),
                        page = call.parameters["page"]?.toLong() ?: 0,
                        size = call.parameters["size"]?.toInt() ?: 10,
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