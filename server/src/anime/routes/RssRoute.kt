package dev.sunriseydy.acgn.anime.routes

import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.service.RssService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.rssRoutes() {
    val rssService = RssService()
    route("/rss") {
        get {
            call.respond(rssService.getRssList())
        }
        get("/{id}") {
            call.respond(rssService.getRssById(call.parameters["id"]!!.toULong()))
        }
        put("/{id}") {
            call.respond(rssService.updateRss(call.receive<Rss>()))
        }
        post {
            call.respond(rssService.createRss(call.receive<Rss>()))
        }
    }
}