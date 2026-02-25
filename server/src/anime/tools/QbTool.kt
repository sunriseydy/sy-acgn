package dev.sunriseydy.acgn.server.anime.tools

import anime.tools.torrent.TorrentParser
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import dev.sunriseydy.acgn.anime.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.dto.TorrentAdd
import dev.sunriseydy.acgn.anime.enums.AnimeModuleError
import dev.sunriseydy.acgn.base.exception.MessageException
import dev.sunriseydy.acgn.server.anime.tools.QbTool.Companion.MAX_RETRY_COUNT
import dev.sunriseydy.acgn.server.anime.tools.qbittorrent.model.QbRssArticle
import dev.sunriseydy.acgn.server.anime.tools.qbittorrent.model.QbRssAutoDownloadingRule
import dev.sunriseydy.acgn.server.anime.tools.qbittorrent.model.QbRssItem
import dev.sunriseydy.acgn.server.anime.tools.qbittorrent.model.TorrentInfo
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlin.time.Instant as KtInstant
import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger { }

/**
 * qBittorrent API 客户端工具类
 *
 * 封装与 qBittorrent WebUI API 的交互，包括 Torrent 管理和 RSS 订阅管理。
 * 自动处理登录认证和 Cookie 维护。
 *
 * @author SunriseYDY
 * @date 2024-07-20 16:03
 */
class QbTool {

    companion object {
        /** qBittorrent Cookie 名称 */
        private const val QB_COOKIE_NAME = "SID"

        /** 请求失败后最大重试次数，防止无限递归 */
        private const val MAX_RETRY_COUNT = 3
    }

    private val userName = AnimeModuleAppConfig.QbUserName.configValue
    private val password = AnimeModuleAppConfig.QbPassword.configValue
    private val apiBaseUrl = AnimeModuleAppConfig.QbApiBaseUrl.configValue

    init {
        checkNotNull(apiBaseUrl) { "Qb api base url is null" }
    }

    private val httpClient = HttpClientFactory.buildHttpClient(logLevel = LogLevel.BODY) {
        install(HttpCookies)
        expectSuccess = false
    }

    /**
     * RSS 数据缓存
     * - 最大容量：100 个条目
     * - 过期策略：写入后 5 分钟过期
     * - 启用统计：recordStats()
     */
    private val rssItemsCache: LoadingCache<String, Map<String, QbRssItem>> = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build { key ->
            // 缓存加载器：从 key 中解析 withData 参数
            val withData = key.removePrefix("rss_items_").toBoolean()
            runBlocking {
                invoke {
                    httpClient.get(urlString = apiBaseUrl!!) {
                        url {
                            appendPathSegments(QbUrl.QB_RSS_ITEMS)
                            parameters.append("withData", withData.toString())
                        }
                    }
                }.body()
            }
        }

    private suspend fun checkCookie() =
        httpClient.cookies(apiBaseUrl!!)
            .any { it.name == QB_COOKIE_NAME }

    private suspend fun login() {
        httpClient.submitForm(
            url = apiBaseUrl + QbUrl.QB_LOGIN,
            formParameters = parameters {
                userName?.let { append("username", it) }
                password?.let { append("password", it) }
            }
        )
        if (!checkCookie()) {
            throw MessageException(AnimeModuleError.QB_LOGIN_FAILED)
        }
        logger.info { "qBittorrent 登录成功" }
    }

    /**
     * 执行带自动认证的 HTTP 请求
     *
     * 自动检查 Cookie 有效性，失败时重试登录（最多 [MAX_RETRY_COUNT] 次）。
     *
     * @param retryCount 当前重试次数
     * @param block 要执行的 HTTP 请求块
     * @return HTTP 响应
     */
    suspend fun invoke(retryCount: Int = 0, block: suspend () -> HttpResponse): HttpResponse {
        if (!checkCookie()) {
            login()
        }
        val response = block()
        if (response.status.isSuccess()) {
            logger.debug { "qBittorrent 请求成功: ${response.request.url}" }
            return response
        } else {
            if (response.status == HttpStatusCode.Forbidden && retryCount < MAX_RETRY_COUNT) {
                logger.warn { "qBittorrent 请求被拒绝 (403)，尝试重新登录 (重试 ${retryCount + 1}/$MAX_RETRY_COUNT)" }
                login()
                return invoke(retryCount + 1, block)
            } else {
                logger.error { "qBittorrent 请求失败: ${response.status} (重试次数: $retryCount)" }
                throw MessageException(AnimeModuleError.QB_REQUEST_FAILED)
            }
        }
    }

    suspend fun addTorrent(torrentAdd: TorrentAdd): String {
        val hash: String
        var bytes: ByteArray? = null
        if (torrentAdd.url.startsWith("http")) {
            val response = httpClient.get(torrentAdd.url)
            if (!response.status.isSuccess()) {
                throw MessageException(AnimeModuleError.QB_DOWNLOAD_TORRENT_FAILED)
            }
            bytes = response.body()
            val torrent = TorrentParser.parseTorrent(ByteArrayInputStream(bytes))
            if (torrent == null) {
                throw MessageException(AnimeModuleError.QB_PARSE_TORRENT_FAILED)
            } else {
                hash = torrent.info_hash
            }
        } else if (torrentAdd.url.startsWith("magnet:")) {
            hash =
                this.extractInfoHash(torrentAdd.url) ?: throw MessageException(AnimeModuleError.QB_PARSE_MAGNET_FAILED)
        } else {
            throw MessageException(AnimeModuleError.QB_PARSE_HASH_FAILED)
        }
        invoke {
            httpClient.submitFormWithBinaryData(
                url = apiBaseUrl + QbUrl.QB_TORRENT_ADD,
                formData = formData {
                    bytes?.let { append("torrents", it) } ?: append("urls", torrentAdd.url)
                    torrentAdd.category?.let { append("category", it) }
                    append("autoTMM", torrentAdd.autoTMM.toString())
                }
            )
        }
        logger.info { "Torrent 添加成功: hash=$hash" }
        return hash.lowercase()
    }

    suspend fun getTorrentInfo(hash: String): TorrentInfo =
        invoke {
            httpClient.get(urlString = apiBaseUrl!!) {
                url {
                    appendPathSegments(QbUrl.QB_TORRENT_DETAIL)
                    parameters.append("hash", hash)
                }
            }
        }.body()

    fun extractInfoHash(magnetLink: String): String? {
        val params = magnetLink.substringAfter("magnet:?").split("&")
        for (param in params) {
            val keyValue = param.split("=")
            if (keyValue.size == 2 && keyValue[0] == "xt" && keyValue[1].startsWith("urn:btih:")) {
                return URLDecoder.decode(keyValue[1].substringAfter("urn:btih:"), "UTF-8")
            }
        }
        return null
    }

    // RSS API Methods

    /**
     * 添加 RSS 文件夹
     * @param path 文件夹路径，如 "folder1" 或 "folder1/subfolder"
     */
    suspend fun addRssFolder(path: String) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_ADD_FOLDER,
                formParameters = parameters {
                    append("path", path)
                }
            )
        }
    }

    /**
     * 添加 RSS 订阅源
     * @param url RSS 订阅源 URL
     * @param path 可选的文件夹路径，如 "folder1" 或 "folder1/subfolder/feed"
     */
    suspend fun addRssFeed(url: String, path: String? = null) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_ADD_FEED,
                formParameters = parameters {
                    append("url", url)
                    path?.let { append("path", it) }
                }
            )
        }
        // 添加后清除缓存
        invalidateRssCache()
    }

    /**
     * 删除 RSS 项（订阅源或文件夹）
     * @param path 要删除的项路径
     */
    suspend fun removeRssItem(path: String) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_REMOVE_ITEM,
                formParameters = parameters {
                    append("path", path)
                }
            )
        }
        // 删除后清除缓存
        invalidateRssCache()
    }

    /**
     * 移动/重命名 RSS 项
     * @param itemPath 原路径
     * @param destPath 目标路径
     */
    suspend fun moveRssItem(itemPath: String, destPath: String) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_MOVE_ITEM,
                formParameters = parameters {
                    append("itemPath", itemPath)
                    append("destPath", destPath)
                }
            )
        }
        // 移动/重命名后清除缓存
        invalidateRssCache()
    }

    /**
     * 获取所有 RSS 订阅项及其内容（使用缓存）
     * @param withData 是否包含文章数据，默认为 false
     */
    suspend fun getRssItems(withData: Boolean = false): Map<String, QbRssItem> {
        val cacheKey = "rss_items_$withData"
        return rssItemsCache.get(cacheKey)!!
    }

    /**
     * 清除 RSS 缓存
     */
    fun invalidateRssCache() {
        rssItemsCache.invalidateAll()
        logger.debug { "RSS 缓存已清除" }
    }

    /**
     * 获取缓存统计信息
     */
    fun logCacheStats() {
        val stats = rssItemsCache.stats()
        logger.info { "RSS Cache Stats - Hits: ${stats.hitCount()}, Misses: ${stats.missCount()}, Hit Rate: ${"%.2f".format(stats.hitRate() * 100)}%" }
    }

    /**
     * 刷新 RSS 项
     * @param itemPath RSS 项路径，为空则刷新所有
     */
    suspend fun refreshRssItem(itemPath: String? = null) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_REFRESH_ITEM,
                formParameters = parameters {
                    itemPath?.let { append("itemPath", it) }
                }
            )
        }
        // 刷新后清除缓存，确保下次获取最新数据
        invalidateRssCache()
    }

    /**
     * 设置 RSS 自动下载规则
     * @param ruleName 规则名称
     * @param ruleDef 规则定义
     */
    suspend fun setRssRule(ruleName: String, ruleDef: QbRssAutoDownloadingRule) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_SET_RULE,
                formParameters = parameters {
                    append("ruleName", ruleName)
                    append("ruleDef", ruleDef.toJsonString())
                }
            )
        }
    }

    /**
     * 重命名 RSS 自动下载规则
     * @param ruleName 原规则名称
     * @param newRuleName 新规则名称
     */
    suspend fun renameRssRule(ruleName: String, newRuleName: String) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_RENAME_RULE,
                formParameters = parameters {
                    append("ruleName", ruleName)
                    append("newRuleName", newRuleName)
                }
            )
        }
    }

    /**
     * 删除 RSS 自动下载规则
     * @param ruleName 规则名称
     */
    suspend fun removeRssRule(ruleName: String) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_REMOVE_RULE,
                formParameters = parameters {
                    append("ruleName", ruleName)
                }
            )
        }
    }

    /**
     * 获取所有 RSS 自动下载规则
     */
    suspend fun getRssRules(): Map<String, QbRssAutoDownloadingRule> =
        invoke {
            httpClient.get(apiBaseUrl + QbUrl.QB_RSS_RULES)
        }.body()

    /**
     * 获取匹配指定规则的文章
     * @param ruleName 规则名称
     */
    suspend fun getMatchingArticles(ruleName: String): Map<String, List<String>> =
        invoke {
            httpClient.get(urlString = apiBaseUrl!!) {
                url {
                    appendPathSegments(QbUrl.QB_RSS_MATCHING_ARTICLES)
                    parameters.append("ruleName", ruleName)
                }
            }
        }.body()

    /**
     * 标记 RSS 文章为已读
     * @param itemPath RSS 项路径，如 "folder/feed"
     * @param articleId 可选的文章 ID，如果不提供则标记整个订阅源为已读
     */
    suspend fun markAsRead(itemPath: String, articleId: String? = null) {
        invoke {
            httpClient.submitForm(
                url = apiBaseUrl + QbUrl.QB_RSS_MARK_AS_READ,
                formParameters = parameters {
                    append("itemPath", itemPath)
                    articleId?.let { append("articleId", it) }
                }
            )
        }
        // 标记已读后清除缓存，确保下次查询返回最新的已读状态
        invalidateRssCache()
    }

    /**
     * 获取 RSS 列表（已转换为 Rss DTO 格式）
     * @param withData 是否包含文章数据
     */
    suspend fun getRssList(withData: Boolean = false): List<Rss> {
        val items = getRssItems(withData)
        return convertQbRssItemsToRssList(items)
    }

    /**
     * 将 qBittorrent RSS 数据转换为 Rss 列表
     */
    private fun convertQbRssItemsToRssList(data: Map<String, QbRssItem>): List<Rss> {
        val result = mutableListOf<Rss>()
        data.forEach { (key, value) ->
            // 只处理 RSS 订阅源（有 uid 字段的）
            if (value.uid != null) {
                val id = key.hashCode().toULong()
                val unreadCount = value.articles?.count { (it.isRead ?: false) == false }?.toLong() ?: 0L

                result.add(
                    Rss(
                        id = id,
                        link = value.uid ?: "",
                        title = value.title ?: key,
                        description = value.lastBuildDate,
                        lastFetchAt = Clock.System.now(),
                    ).apply {
                        this.unreadCount = unreadCount
                    }
                )
            }
        }
        return result
    }

    /**
     * 获取分页的 RSS 文章列表（已转换为 RssItem DTO）
     * @param rssId 订阅源 ID（可选，为 null 时获取所有订阅源的文章）
     * @param isRead 已读状态（可选，为 null 时获取所有文章）
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     */
    suspend fun getRssItemsByRssIdOrIsRead(
        rssId: ULong?,
        isRead: Boolean?,
        page: Long,
        size: Int
    ): List<RssItem> {
        // 1. 获取所有 RSS 数据（包含文章）
        val qbRssItems = getRssItems(withData = true)

        // 2. 构建 rssId 到路径的映射
        val rssIdToPath = mutableMapOf<ULong, String>()
        qbRssItems.forEach { (path, item) ->
            if (item.uid != null) {
                rssIdToPath[path.hashCode().toULong()] = path
            }
        }

        // 3. 收集所有文章
        val allArticles = mutableListOf<Pair<String, QbRssArticle>>() // Pair<路径, 文章>
        qbRssItems.forEach { (path, item) ->
            if (item.uid != null && item.articles != null) {
                item.articles.forEach { article ->
                    allArticles.add(path to article)
                }
            }
        }

        // 4. 过滤：根据 rssId
        val filteredByRssId = if (rssId != null) {
            val targetPath = rssIdToPath[rssId]
            if (targetPath != null) {
                allArticles.filter { it.first == targetPath }
            } else {
                emptyList()
            }
        } else {
            allArticles
        }

        // 5. 过滤：根据 isRead
        val filteredByRead = if (isRead != null) {
            filteredByRssId.filter { (it.second.isRead ?: false) == isRead }
        } else {
            filteredByRssId
        }

        // 6. 排序：按发布时间倒序（最新的在前）
        val sorted = filteredByRead.sortedByDescending {
            parseArticleDate(it.second.date)
        }

        // 7. 分页
        val offset = ((page - 1) * size).toInt()
        val paged = sorted.drop(offset).take(size)

        // 8. 转换为 RssItem DTO
        return paged.map { (path, article) ->
            convertQbRssArticleToRssItem(path, article)
        }
    }

    /**
     * 将 qBittorrent RSS 文章转换为 RssItem DTO
     */
    private fun convertQbRssArticleToRssItem(path: String, article: QbRssArticle): RssItem {
        val rssId = path.hashCode().toULong()
        val articleId = article.id ?: UUID.randomUUID().toString()
        val publishedAt = parseArticleDate(article.date)

        return RssItem(
            id = articleId,
            rssId = rssId,
            link = article.link ?: "",
            guid = articleId,
            title = article.title ?: "Untitled",
            description = article.description,
            content = null,
            torrent = article.torrentURL ?: article.link ?: "",
            isRead = article.isRead ?: false,
            publishedAt = publishedAt,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
    }

    /**
     * 解析文章日期，支持多种格式
     */
    private fun parseArticleDate(dateStr: String?): KtInstant {
        if (dateStr.isNullOrBlank()) {
            return Clock.System.now()
        }

        // 尝试多种日期格式
        val formatters = listOf(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
        )

        for (formatter in formatters) {
            try {
                return OffsetDateTime.parse(dateStr, formatter).toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) }
            } catch (_: DateTimeParseException) {
                // 继续尝试下一个格式
            }
        }

        // 所有格式都失败，返回当前时间
        logger.warn { "无法解析日期: $dateStr, 使用当前时间" }
        return Clock.System.now()
    }
}

/** qBittorrent API 路径常量 */
object QbUrl {
    const val QB_LOGIN = "/api/v2/auth/login"
    const val QB_TORRENT_ADD = "/api/v2/torrents/add"
    const val QB_TORRENT_DETAIL = "/api/v2/torrents/properties"
    const val QB_RSS_ADD_FOLDER = "/api/v2/rss/addFolder"
    const val QB_RSS_ADD_FEED = "/api/v2/rss/addFeed"
    const val QB_RSS_REMOVE_ITEM = "/api/v2/rss/removeItem"
    const val QB_RSS_MOVE_ITEM = "/api/v2/rss/moveItem"
    const val QB_RSS_ITEMS = "/api/v2/rss/items"
    const val QB_RSS_REFRESH_ITEM = "/api/v2/rss/refreshItem"
    const val QB_RSS_SET_RULE = "/api/v2/rss/setRule"
    const val QB_RSS_RENAME_RULE = "/api/v2/rss/renameRule"
    const val QB_RSS_REMOVE_RULE = "/api/v2/rss/removeRule"
    const val QB_RSS_RULES = "/api/v2/rss/rules"
    const val QB_RSS_MATCHING_ARTICLES = "/api/v2/rss/matchingArticles"
    const val QB_RSS_MARK_AS_READ = "/api/v2/rss/markAsRead"
}