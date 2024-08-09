package dev.sunriseydy.acgn.server.anime.tools.tmdb.api

import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.AppendResponse
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbEpisodeDetail
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbExternalIds
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.endPointV3
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterAppendResponses
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterIncludeImageLanguage
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterLanguage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class TmdbShowEpisodesApi(private val client: HttpClient) {

    suspend fun getDetails(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        language: String? = null,
        appendResponses: Iterable<AppendResponse>? = null,
        includeImageLanguages: String? = null
    ): TmdbEpisodeDetail = client.get {
        endPointEpisode(showId, seasonNumber, episodeNumber)
        parameterLanguage(language)
        parameterAppendResponses(appendResponses)
        parameterIncludeImageLanguage(includeImageLanguages)
    }.body()

    suspend fun getExternalIds(showId: Int, seasonNumber: Int, episodeNumber: Int): TmdbExternalIds = client.get {
        endPointEpisode(showId, seasonNumber, episodeNumber, "external_ids")
    }.body()

    private fun HttpRequestBuilder.endPointEpisode(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        vararg paths: String
    ) {
        endPointV3(
            "tv",
            showId.toString(),
            "season",
            seasonNumber.toString(),
            "episode",
            episodeNumber.toString(),
            *paths
        )
    }
}
