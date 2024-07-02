package dev.sunriseydy.acgn.anime.routes

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.service.RssService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.rssRoutes() {
    val rssService = RssService()
    route("/rss") {
        get {
            call.respond(rssService.getRssList())
        }
        put("/{id}") {
            call.respond(rssService.saveRss(call.receive<Rss>().apply { id = call.parameters["id"]!!.toULong() }))
        }
        post {
            call.respond(rssService.createRss(call.receive<Rss>().link))
        }
        delete("/{id}") {
            call.respond(rssService.removeRss(call.parameters["id"]!!.toULong()))
        }
        route("/item") {
            get {
                call.respond(rssService.selectRssItemByRssIdOrIsRead(
                    rssId = call.parameters["rssId"]?.toULong(),
                    isRead = call.parameters["isRead"]?.toBoolean(),
                ))
            }
            put("/read") {
                call.respond(rssService.updateRssItemReadByIdOrRssId(
                    id = call.parameters["id"]?.let { UUID.fromString(it) },
                    rssId = call.parameters["rssId"]?.toULong(),
                ))
            }
        }
    }
}