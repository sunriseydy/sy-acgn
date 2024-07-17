package dev.sunriseydy.acgn.anime.tools.tmdb.api

import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbBelongsToCollection
import dev.sunriseydy.acgn.anime.tools.tmdb.core.endPointV3
import dev.sunriseydy.acgn.anime.tools.tmdb.core.parameterLanguage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class TmdbCollectionsApi internal constructor(private val client: HttpClient) {

    suspend fun getDetails(collectionId: Int, language: String? = null): TmdbBelongsToCollection = client.get {
        endPointV3("collection", collectionId.toString())
        parameterLanguage(language)
    }.body()
}
