package dev.sunriseydy.acgn.client.anime.service

import androidx.compose.runtime.MutableState
import dev.sunriseydy.acgn.anime.dto.Rss
import dev.sunriseydy.acgn.anime.dto.RssItem
import dev.sunriseydy.acgn.anime.dto.TorrentAdd
import dev.sunriseydy.acgn.client.AppState
import dev.sunriseydy.acgn.client.interfaces.Paging
import dev.sunriseydy.acgn.client.onSuccess
import dev.sunriseydy.acgn.client.onSuccessData

/**
 * @author SunriseYDY
 * @date 2025-02-15 11:36
 */
class RssService(
    private val appState: AppState,
    private val rssList: MutableState<List<Rss>>,
    private val rssItemPager: Paging<RssItem>,
) {
    fun loadRss() {
        appState.api.rss.getAllRss().onSuccessData(appState, onSuccess = { data ->
            rssList.value = data
        })
    }

    fun createRss(
        link: String,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        appState.api.rss.createRss(link)
            .onSuccess(null, onSuccess = onSuccess, onError = onError)
    }

    fun deleteRss(id: ULong, onSuccess: () -> Unit) {
        appState.api.rss.deleteRss(id).onSuccess(appState, onSuccess)
    }

    fun updateRss(
        rss: Rss,
        onSuccess: () -> Unit = { },
        onError: (String) -> Unit = { },
    ) {
        appState.api.rss.saveRss(rss.id, rss)
            .onSuccess(appState, onSuccess, onError)
    }

    fun markRssItemReadByIdOrRssId(
        id: String? = null,
        rssId: ULong? = null
    ) {
        appState.api.rss.markRssItemReadByIdOrRssId(id, rssId).onSuccess(appState, onSuccess = { loadData() })
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
}