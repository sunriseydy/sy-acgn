package dev.sunriseydy.acgn.anime

import dev.sunriseydy.acgn.anime.enums.AnimeMonthType
import dev.sunriseydy.acgn.base.ApiResource
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("anime")
class AnimeModuleResource(val parent: ApiResource = ApiResource()) {
    @Serializable
    @Resource("rss")
    class Rss(val parent: AnimeModuleResource = AnimeModuleResource()) {
        @Serializable
        @Resource("{id}")
        class Id(val parent: Rss = Rss(), val id: ULong)

        @Serializable
        @Resource("fetch")
        class Fetch(val parent: Rss = Rss(), val rssId: ULong? = null)

        @Serializable
        @Resource("item")
        class Item(
            val parent: Rss = Rss(),
            val rssId: ULong? = null,
            val isRead: Boolean? = null,
            val page: Long? = null,
            val size: Int? = null
        ) {
            @Serializable
            @Resource("read")
            class Read(val parent: Item = Item(), val id: String? = null, val rssId: ULong? = null)
        }
    }

    @Serializable
    @Resource("qb")
    class Qb(val parent: AnimeModuleResource = AnimeModuleResource()) {
        @Serializable
        @Resource("torrent")
        class Torrent(val parent: Qb = Qb()) {
            @Serializable
            @Resource("{hash}")
            class Hash(val parent: Torrent = Torrent(), val hash: String)
        }
    }

    @Serializable
    @Resource("anime")
    class Anime(val parent: AnimeModuleResource = AnimeModuleResource()) {
        @Serializable
        @Resource("{animeId}")
        class Id(val parent: Anime = Anime(), val animeId: ULong)

        @Serializable
        @Resource("name")
        class Name(val parent: Anime = Anime(), val name: String? = null)

        @Serializable
        @Resource("cache")
        class Cache(val parent: Anime = Anime())

        @Serializable
        @Resource("refresh")
        class Refresh(val parent: Anime = Anime())

        @Serializable
        @Resource("season")
        class Season(val parent: Anime = Anime()) {
            @Serializable
            @Resource("{id}")
            class Id(val parent: Season = Season(), val id: ULong)

            @Serializable
            @Resource("years")
            class Years(val parent: Season = Season())

            @Serializable
            @Resource("by-anime-id")
            class ByAnimeId(val parent: Season = Season(), val animeId: ULong)

            @Serializable
            @Resource("by-year-and-month-type")
            class ByYearAndMonth(
                val parent: Season = Season(),
                val year: Int,
                val monthType: AnimeMonthType? = null
            )

            @Serializable
            @Resource("section-map")
            class SectionMap(val parent: Season = Season())

            @Serializable
            @Resource("episode")
            class Episode(val parent: Season = Season()) {
                @Serializable
                @Resource("{episodeId}")
                class Id(val parent: Episode = Episode(), val episodeId: ULong)
            }
        }

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

        @Serializable
        @Resource("file")
        class File(val parent: Anime = Anime()) {
            @Serializable
            @Resource("season-file")
            class SeasonFile(val parent: File = File())
        }
    }
}
