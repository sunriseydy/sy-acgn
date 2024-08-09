package dev.sunriseydy.acgn.server.anime.tools.tmdb.url

import dev.sunriseydy.acgn.server.anime.tools.tmdb.TmdbWebConfig

object TmdbAuthenticationUrlBuilder {

    fun buildAuthorizationUrl(requestToken: String, redirectTo: String) =
        "${TmdbWebConfig.BASE_WEBSITE_URL}/authenticate/$requestToken?redirect_to=$redirectTo"
}
