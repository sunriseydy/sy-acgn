package dev.sunriseydy.acgn.server.anime.tools.tmdb

import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbAccountApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbAuthenticationApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbCertificationsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbChangesApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbCollectionsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbCompaniesApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbConfigurationApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbCreditsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbDiscoverApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbFindApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbGenresApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbGuestSessionsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbKeywordsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbListsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbMoviesApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbNetworksApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbPeopleApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbReviewsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbSearchApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbShowApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbShowEpisodeGroupsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbShowEpisodesApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbShowSeasonsApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.api.TmdbTrendingApi
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.HttpClientFactory
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.interceptRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

fun Tmdb3(block: TmdbClientConfig.() -> Unit): Tmdb3 {
    val config = TmdbClientConfig().apply(block)
    return Tmdb3(config)
}

class Tmdb3 internal constructor(private val config: TmdbClientConfig) {

    constructor(tmdbApiKey: String) : this(TmdbClientConfig.withKey(tmdbApiKey))

    init {
        requireNotNull(config.tmdbApiKey) {
            "TMDB API key unavailable. Set the tmdbApiKey field in the class TmdbClientConfig when instantiate the TMDB client."
        }
    }

    private val client: HttpClient by lazy {
        HttpClientFactory.buildHttpClient(TmdbVersion.V3, config).apply {
            interceptRequest {
                it.parameter(TmdbUrlParameter.API_KEY, config.tmdbApiKey)

                val pathSegments = it.url.pathSegments.toSet()
                if (pathSegments.contains("account") || pathSegments.contains("authentication")) {
                    config.tmdbAuthCredentials?.sessionIdProvider?.invoke()?.let { sessionId ->
                        it.parameter(TmdbUrlParameter.SESSION_ID, sessionId)
                    }
                }

                config.tmdbAuthCredentials?.guestSessionIdProvider?.invoke()?.let { sessionId ->
                    it.parameter(TmdbUrlParameter.GUEST_SESSION_ID, sessionId)
                }
            }
        }
    }

    val account: TmdbAccountApi by buildApi(::TmdbAccountApi)
    val authentication by buildApi(::TmdbAuthenticationApi)
    val certifications by buildApi(::TmdbCertificationsApi)
    val changes by buildApi(::TmdbChangesApi)
    val collections by buildApi(::TmdbCollectionsApi)
    val companies by buildApi(::TmdbCompaniesApi)
    val configuration by buildApi(::TmdbConfigurationApi)
    val credits by buildApi(::TmdbCreditsApi)
    val discover by buildApi(::TmdbDiscoverApi)
    val find by buildApi(::TmdbFindApi)
    val genres by buildApi(::TmdbGenresApi)
    val guestSessions by buildApi(::TmdbGuestSessionsApi)
    val keywords by buildApi(::TmdbKeywordsApi)
    val lists by buildApi(::TmdbListsApi)
    val movies by buildApi(::TmdbMoviesApi)
    val networks by buildApi(::TmdbNetworksApi)
    val trending by buildApi(::TmdbTrendingApi)
    val people by buildApi(::TmdbPeopleApi)
    val reviews by buildApi(::TmdbReviewsApi)
    val search by buildApi(::TmdbSearchApi)
    val show by buildApi(::TmdbShowApi)
    val showSeasons by buildApi(::TmdbShowSeasonsApi)
    val showEpisodes by buildApi(::TmdbShowEpisodesApi)
    val showEpisodeGroups by buildApi(::TmdbShowEpisodeGroupsApi)

    private inline fun <T> buildApi(crossinline builder: (HttpClient) -> T) = lazy {
        builder(client)
    }
}
