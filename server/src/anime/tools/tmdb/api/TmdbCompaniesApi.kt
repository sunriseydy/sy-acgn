package dev.sunriseydy.acgn.server.anime.tools.tmdb.api

import dev.sunriseydy.acgn.server.anime.tools.tmdb.model.TmdbCompanyDetail
import dev.sunriseydy.acgn.server.anime.tools.tmdb.core.endPointV3
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class TmdbCompaniesApi internal constructor(private val client: HttpClient) {

    suspend fun getDetails(companyId: Int): TmdbCompanyDetail = client.get {
        endPointV3("company", companyId.toString())
    }.body()
}
