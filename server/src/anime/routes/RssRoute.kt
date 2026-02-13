package dev.sunriseydy.acgn.server.anime.routes

import dev.sunriseydy.acgn.anime.AnimeModuleResource
import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.server.anime.tools.QbTool
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Route.rssRoutes() {
    get<AnimeModuleResource.Qb.Torrent.Hash> { resource ->
        call.respond(Result(data = QbTool().getTorrentInfo(resource.hash)))
    }
    post<AnimeModuleResource.Qb.Torrent> {
        call.respond(Result(data = QbTool().addTorrent(call.receive())))
    }

    // qBittorrent RSS API

    // 获取 RSS 列表（已转换为 Rss DTO）
    get<AnimeModuleResource.Qb.Rss.List> { resource ->
        call.respond(Result(data = application.dependencies.resolve<QbTool>().getRssList(resource.withData)))
    }

    // 获取 RSS 文章列表（已转换为 RssItem DTO，支持分页）
    get<AnimeModuleResource.Qb.Rss.Articles> { resource ->
        call.respond(Result(data = application.dependencies.resolve<QbTool>().getRssItemsByRssIdOrIsRead(
            rssId = resource.rssId,
            isRead = resource.isRead,
            page = resource.page,
            size = resource.size
        )))
    }

    // RSS 文件夹管理
    post<AnimeModuleResource.Qb.Rss.Folder> {
        val path = call.receive<JsonObject>().getValue("path").jsonPrimitive.content
        application.dependencies.resolve<QbTool>().addRssFolder(path)
        call.respond(Result<Unit>())
    }

    // RSS 订阅源管理
    post<AnimeModuleResource.Qb.Rss.Feed> {
        val body = call.receive<JsonObject>()
        val url = body.getValue("url").jsonPrimitive.content
        val path = body["path"]?.jsonPrimitive?.content
        application.dependencies.resolve<QbTool>().addRssFeed(url, path)
        call.respond(Result<Unit>())
    }

    // RSS 项管理
    get<AnimeModuleResource.Qb.Rss.Item> { resource ->
        call.respond(Result(data = application.dependencies.resolve<QbTool>().getRssItems(resource.withData)))
    }

    post<AnimeModuleResource.Qb.Rss.Item.Remove> {
        val path = call.receive<JsonObject>().getValue("path").jsonPrimitive.content
        application.dependencies.resolve<QbTool>().removeRssItem(path)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Item.Move> {
        val body = call.receive<JsonObject>()
        val itemPath = body.getValue("itemPath").jsonPrimitive.content
        val destPath = body.getValue("destPath").jsonPrimitive.content
        application.dependencies.resolve<QbTool>().moveRssItem(itemPath, destPath)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Item.Refresh> {
        val itemPath = call.receive<JsonObject>()["itemPath"]?.jsonPrimitive?.content
        application.dependencies.resolve<QbTool>().refreshRssItem(itemPath)
        call.respond(Result<Unit>())
    }

    // RSS 自动下载规则管理
    get<AnimeModuleResource.Qb.Rss.Rule> {
        call.respond(Result(data = application.dependencies.resolve<QbTool>().getRssRules()))
    }

    post<AnimeModuleResource.Qb.Rss.Rule> {
        val body = call.receive<JsonObject>()
        val ruleName = body.getValue("ruleName").jsonPrimitive.content
        val ruleDef = kotlinx.serialization.json.Json.decodeFromJsonElement(
            dev.sunriseydy.acgn.server.anime.tools.QbRssAutoDownloadingRule.serializer(),
            body.getValue("ruleDef")
        )
        application.dependencies.resolve<QbTool>().setRssRule(ruleName, ruleDef)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Rule.Rename> {
        val body = call.receive<JsonObject>()
        val ruleName = body.getValue("ruleName").jsonPrimitive.content
        val newRuleName = body.getValue("newRuleName").jsonPrimitive.content
        application.dependencies.resolve<QbTool>().renameRssRule(ruleName, newRuleName)
        call.respond(Result<Unit>())
    }

    post<AnimeModuleResource.Qb.Rss.Rule.Remove> {
        val ruleName = call.receive<JsonObject>().getValue("ruleName").jsonPrimitive.content
        application.dependencies.resolve<QbTool>().removeRssRule(ruleName)
        call.respond(Result<Unit>())
    }

    get<AnimeModuleResource.Qb.Rss.Rule.MatchingArticles> { resource ->
        call.respond(Result(data = application.dependencies.resolve<QbTool>().getMatchingArticles(resource.ruleName)))
    }

    // RSS 标记已读
    post<AnimeModuleResource.Qb.Rss.MarkAsRead> {
        val body = call.receive<JsonObject>()
        val itemPath = body.getValue("itemPath").jsonPrimitive.content
        val articleId = body["articleId"]?.jsonPrimitive?.content
        application.dependencies.resolve<QbTool>().markAsRead(itemPath, articleId)
        call.respond(Result<Unit>())
    }
}