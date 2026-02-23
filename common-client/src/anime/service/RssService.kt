package dev.sunriseydy.acgn.client.anime.service

import androidx.compose.runtime.MutableState
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.dto.TorrentAdd
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.base.api.onSuccess
import dev.sunriseydy.acgn.client.base.api.onSuccessData
import dev.sunriseydy.acgn.client.base.interfaces.Paging

/**
 * RSS 服务层
 *
 * 封装 RSS 相关的业务逻辑，协调 API 调用和 UI 状态更新。
 *
 * @param appState 应用全局状态
 * @param rssList RSS 订阅列表状态
 * @param rssItemPager RSS 条目分页器
 *
 * @author SunriseYDY
 * @date 2025-02-15 11:36
 */
class RssService(
    private val appState: AppState,
    private val rssList: MutableState<List<Rss>>,
    private val rssItemPager: Paging<RssItem>,
) {
    fun loadRss() {
        // 从 qBittorrent 加载 RSS 订阅列表（后端已转换为 Rss DTO）
        appState.api.rss.getQbRssList(withData = false).onSuccessData(appState, onSuccess = { data ->
            rssList.value = data
        })
    }

    fun createRss(
        link: String,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        // 使用 qBittorrent API 添加 RSS 订阅
        appState.api.rss.addQbRssFeed(link)
            .onSuccess(null, onSuccess = onSuccess, onError = onError)
    }

    fun deleteRss(id: ULong, onSuccess: () -> Unit) {
        // 使用 qBittorrent API 删除 RSS
        // id 在这里是 RSS 路径的哈希值，需要从 rssList 中找到对应的路径
        val rss = rssList.value.find { it.id == id }
        if (rss != null) {
            appState.api.rss.removeQbRssItem(rss.title).onSuccess(appState, onSuccess)
        }
    }

    fun updateRss(
        rss: Rss,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        // qBittorrent 不直接支持更新，需要先删除再添加
        // 或者使用 moveRssItem 重命名
        val oldTitle = rssList.value.find { it.id == rss.id }?.title
        if (oldTitle != null && oldTitle != rss.title) {
            appState.api.rss.moveQbRssItem(oldTitle, rss.title)
                .onSuccess(appState, onSuccess, onError)
        }
    }

    fun markRssItemReadByIdOrRssId(
        id: String? = null,
        rssId: ULong? = null
    ) {
        // 使用 qBittorrent 的 markAsRead API
        if (id != null) {
            // 标记特定文章为已读
            // 需要找到对应的 RSS 订阅源以获取路径
            val rss = if (rssId != null) {
                rssList.value.find { it.id == rssId }
            } else {
                // 如果没有提供 rssId，尝试从当前选择的 RSS 中查找
                // 这里逻辑可能需要优化，但在 UI 中通常是选中了某个 RSS 后操作
                null
            }
            
            if (rss != null) {
                appState.api.rss.markQbRssAsRead(rss.title, id).onSuccess(appState, onSuccess = { loadData() })
            }
        } else if (rssId != null) {
            // 标记整个订阅源为已读
            val rss = rssList.value.find { it.id == rssId }
            if (rss != null) {
                appState.api.rss.markQbRssAsRead(rss.title).onSuccess(appState, onSuccess = { loadData() })
            }
        } else {
            // 标记所有为已读 - qBittorrent 需要逐个标记
            rssList.value.forEach { rss ->
                appState.api.rss.markQbRssAsRead(rss.title).onSuccess(appState)
            }
            loadData()
        }
    }

    fun loadRssItem() {
        rssItemPager.loadInit()
    }

    fun loadMoreRssItem() {
        rssItemPager.loadNext()
    }

    fun loadData() {
        loadRss()
        loadRssItem()
    }

    fun download(link: String, onSuccess: () -> Unit) {
        appState.api.rss.addQbTorrent(TorrentAdd(url = link)).onSuccess(appState, onSuccess)
    }

    fun refreshRss(path: String? = null) {
        appState.api.rss.refreshQbRssItem(path).onSuccess(appState) {
            loadData()
        }
    }
}