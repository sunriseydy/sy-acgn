package dev.sunriseydy.acgn.anime

import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.base.ApiResource
import io.ktor.resources.*
import kotlinx.serialization.Serializable

/**
 * Anime 模块 API 路由资源定义
 *
 * 定义了动画模块下所有可用的 RESTful API 路由。
 * 使用 Ktor Resources 插件实现类型安全路由。
 *
 * 路径结构:
 * - `/api/anime/qb` - qBittorrent 相关 API
 * - `/api/anime/anime` - 动画数据管理 API
 *
 * @author SunriseYDY
 * @date 2024-07-16
 */
@Serializable
@Resource("anime")
class AnimeModuleResource(val parent: ApiResource = ApiResource()) {

    /** qBittorrent 相关 API: `/api/anime/qb` */
    @Serializable
    @Resource("qb")
    class Qb(val parent: AnimeModuleResource = AnimeModuleResource()) {

        /** Torrent 管理: `/api/anime/qb/torrent` */
        @Serializable
        @Resource("torrent")
        class Torrent(val parent: Qb = Qb()) {
            @Serializable
            @Resource("{hash}")
            class Hash(val parent: Torrent = Torrent(), val hash: String)
        }

        /** RSS 订阅管理: `/api/anime/qb/rss` */
        @Serializable
        @Resource("rss")
        class Rss(val parent: Qb = Qb()) {
            @Serializable
            @Resource("list")
            class List(val parent: Rss = Rss(), val withData: Boolean = false)

            @Serializable
            @Resource("articles")
            class Articles(
                val parent: Rss = Rss(),
                val rssId: ULong? = null,
                val isRead: Boolean? = null,
                val page: Long = 1,
                val size: Int = 50
            )

            @Serializable
            @Resource("folder")
            class Folder(val parent: Rss = Rss())

            @Serializable
            @Resource("feed")
            class Feed(val parent: Rss = Rss())

            @Serializable
            @Resource("item")
            class Item(val parent: Rss = Rss(), val withData: Boolean = false) {
                @Serializable
                @Resource("remove")
                class Remove(val parent: Item = Item())

                @Serializable
                @Resource("move")
                class Move(val parent: Item = Item())

                @Serializable
                @Resource("refresh")
                class Refresh(val parent: Item = Item())
            }

            @Serializable
            @Resource("mark-as-read")
            class MarkAsRead(val parent: Rss = Rss())

            @Serializable
            @Resource("rule")
            class Rule(val parent: Rss = Rss()) {
                @Serializable
                @Resource("rename")
                class Rename(val parent: Rule = Rule())

                @Serializable
                @Resource("remove")
                class Remove(val parent: Rule = Rule())

                @Serializable
                @Resource("matching-articles")
                class MatchingArticles(val parent: Rule = Rule(), val ruleName: String)
            }
        }
    }

    /** 动画数据管理: `/api/anime/anime` */
    @Serializable
    @Resource("anime")
    class Anime(val parent: AnimeModuleResource = AnimeModuleResource()) {
        @Serializable
        @Resource("name")
        class Name(val parent: Anime = Anime(), val name: String? = null)

        /** 季度管理: `/api/anime/anime/season` */
        @Serializable
        @Resource("season")
        class Season(val parent: Anime = Anime()) {
            @Serializable
            @Resource("{id}")
            class Id(val parent: Season = Season(), val id: ULong) {
                /** 同步集数: `/api/anime/anime/season/{id}/sync-episodes` */
                @Serializable
                @Resource("sync-episodes")
                class SyncEpisodes(val parent: Id)

                /** 季度下集数列表: `/api/anime/anime/season/{id}/episodes` */
                @Serializable
                @Resource("episodes")
                class Episodes(val parent: Id)
            }

            @Serializable
            @Resource("section-map")
            class SectionMap(
                val parent: Season = Season(),
                val name: String? = null,
                /** 是否强制从数据库查询（跳过缓存，先清除缓存），用于刷新场景 */
                val fromDb: Boolean = false
            )

            /** 剧集管理: `/api/anime/anime/season/episode` */
            @Serializable
            @Resource("episode")
            class Episode(val parent: Season = Season()) {
                @Serializable
                @Resource("{episodeId}")
                class Id(val parent: Episode = Episode(), val episodeId: ULong)
            }
        }

        /** TMDB 集成: `/api/anime/anime/tmdb` */
        @Serializable
        @Resource("tmdb")
        class Tmdb(val parent: Anime = Anime()) {
            @Serializable
            @Resource("search-anime-tv")
            class SearchTv(val parent: Tmdb = Tmdb(), val query: String)

            @Serializable
            @Resource("search-anime-movie")
            class SearchMovie(val parent: Tmdb = Tmdb(), val query: String)

            @Serializable
            @Resource("tv-detail")
            class TvDetail(val parent: Tmdb = Tmdb(), val id: Int)

            @Serializable
            @Resource("season-detail")
            class SeasonDetail(val parent: Tmdb = Tmdb(), val showId: Int, val season: Int)

            @Serializable
            @Resource("movie-detail")
            class MovieDetail(val parent: Tmdb = Tmdb(), val id: Int)
        }

        /** Bangumi 集成: `/api/anime/anime/bangumi` */
        @Serializable
        @Resource("bangumi")
        class Bangumi(val parent: Anime = Anime()) {
            @Serializable
            @Resource("search-anime")
            class SearchAnime(val parent: Bangumi = Bangumi(), val query: String)

            @Serializable
            @Resource("subject-detail")
            class SubjectDetail(val parent: Bangumi = Bangumi(), val id: Int)
        }

        /** 文件管理: `/api/anime/anime/file` */
        @Serializable
        @Resource("file")
        class File(val parent: Anime = Anime()) {
            @Serializable
            @Resource("season-file")
            class SeasonFile(val parent: File = File())
        }
    }
}
