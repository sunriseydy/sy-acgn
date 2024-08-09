package dev.sunriseydy.acgn.server.anime.tools.tmdb.api

import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.AppendResponse
import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbNetwork
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.endPointV3
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.parameterAppendResponses
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class TmdbNetworksApi internal constructor(private val client: HttpClient) {

    suspend fun getDetails(networkId: Int, appendResponses: Iterable<AppendResponse>? = null): TmdbNetwork =
        client.get {
            endPointV3("network", networkId.toString())
            parameterAppendResponses(appendResponses)
        }.body()
}
