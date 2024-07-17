package dev.sunriseydy.acgn.anime.tools.tmdb.core

import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbMediaListItem
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbMovie
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbPerson
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbPersonCredit
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbSearchableListItem
import dev.sunriseydy.acgn.anime.tools.tmdb.model.TmdbShow
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object JsonFactory {

    fun buildJson(): Json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
        isLenient = true
        allowSpecialFloatingPointValues = true
        prettyPrint = false

        val module = SerializersModule {
            polymorphic(TmdbMediaListItem::class, TmdbShow::class, TmdbShow.serializer())
            polymorphic(TmdbMediaListItem::class, TmdbMovie::class, TmdbMovie.serializer())

            polymorphic(TmdbSearchableListItem::class, TmdbShow::class, TmdbShow.serializer())
            polymorphic(TmdbSearchableListItem::class, TmdbMovie::class, TmdbMovie.serializer())
            polymorphic(TmdbSearchableListItem::class, TmdbPerson::class, TmdbPerson.serializer())

            polymorphic(TmdbPersonCredit::class, TmdbPersonCredit.Show::class, TmdbPersonCredit.Show.serializer())
            polymorphic(TmdbPersonCredit::class, TmdbPersonCredit.Movie::class, TmdbPersonCredit.Movie.serializer())
        }
        serializersModule = module
        classDiscriminator = "media_type"
    }
}
