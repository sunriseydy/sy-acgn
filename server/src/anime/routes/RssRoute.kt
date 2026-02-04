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

    // qBittorrent RSS API
    val qbTool = QbTool()

    // 获取 RSS 列表（已转换为 Rss DTO）
    get<AnimeModuleResource.Qb.Rss.List> { resource ->
        call.respond(Result(data = qbTool.getRssList(resource.withData)))
    }

    // 获取 RSS 文章列表（已转换为 RssItem DTO，支持分页）
    get<AnimeModuleResource.Qb.Rss.Articles> { resource ->
        call.respond(Result(data = qbTool.getRssItemsByRssIdOrIsRead(
            rssId = resource.rssId,
            isRead = resource.isRead,
            page = resource.page,
            size = resource.size
        )))
    }

    // RSS 文件夹管理
    post<AnimeModuleResource.Qb.Rss.Folder> {
        val path = call.receive<JsonObject>().getValue("path").jsonPrimitive.content
        qbTool.addRssFolder(path)
        call.respond(Result<Unit>())
    }

    // RSS 订阅源管理
    post<AnimeModuleResource.Qb.Rss.Feed> {
        val body = call.receive<JsonObject>()
        val url = body.getValue("url").jsonPrimitive.content
        val path = body["path"]?.jsonPrimitive?.content
        qbTool.addRssFeed(url, path)
        call.respond(Result<Unit>())
    }

    // RSS 项管理
    get<AnimeModuleResource.Qb.Rss.Item> { resource ->
        call.respond(Result(data = qbTool.getRssItems(resource.withData)))
    }

    post<AnimeModuleResource.Qb.Rss.Item.Remove> {
        val path = call.receive<JsonObject>().getValue("path").jsonPrimitive.content
        qbTool.removeRssItem(path)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Item.Move> {
        val body = call.receive<JsonObject>()
        val itemPath = body.getValue("itemPath").jsonPrimitive.content
        val destPath = body.getValue("destPath").jsonPrimitive.content
        qbTool.moveRssItem(itemPath, destPath)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Item.Refresh> {
        val itemPath = call.receive<JsonObject>()["itemPath"]?.jsonPrimitive?.content
        qbTool.refreshRssItem(itemPath)
        call.respond(Result<Unit>())
    }

    // RSS 自动下载规则管理
    get<AnimeModuleResource.Qb.Rss.Rule> {
        call.respond(Result(data = qbTool.getRssRules()))
    }

    post<AnimeModuleResource.Qb.Rss.Rule> {
        val body = call.receive<JsonObject>()
        val ruleName = body.getValue("ruleName").jsonPrimitive.content
        val ruleDef = kotlinx.serialization.json.Json.decodeFromJsonElement(
            dev.sunriseydy.acgn.server.anime.tools.QbRssAutoDownloadingRule.serializer(),
            body.getValue("ruleDef")
        )
        qbTool.setRssRule(ruleName, ruleDef)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Rule.Rename> {
        val body = call.receive<JsonObject>()
        val ruleName = body.getValue("ruleName").jsonPrimitive.content
        val newRuleName = body.getValue("newRuleName").jsonPrimitive.content
        qbTool.renameRssRule(ruleName, newRuleName)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Rule.Remove> {
        val ruleName = call.receive<JsonObject>().getValue("ruleName").jsonPrimitive.content
        qbTool.removeRssRule(ruleName)
        call.respond(Result<Unit>())
    }

    get<AnimeModuleResource.Qb.Rss.Rule.MatchingArticles> { resource ->
        call.respond(Result(data = qbTool.getMatchingArticles(resource.ruleName)))
    }

    // RSS 标记已读
    post<AnimeModuleResource.Qb.Rss.MarkAsRead> {
        val body = call.receive<JsonObject>()
        val itemPath = body.getValue("itemPath").jsonPrimitive.content
        val articleId = body["articleId"]?.jsonPrimitive?.content
        qbTool.markAsRead(itemPath, articleId)
        call.respond(Result<Unit>())
    }
}