package dev.sunriseydy.acgn.server.anime.tools.tmdb.core

import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for handle the TMDB pattern: 2023-03-05 10:38:01 UTC
 */
internal class TmdbInstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val raw = decoder.decodeString().trim()
        val isoString = when {
            raw.contains('T') -> raw
            raw.endsWith(" UTC") -> raw.removeSuffix(" UTC").replace(' ', 'T') + "Z"
            else -> raw.replace(' ', 'T')
        }
        return Instant.parse(isoString)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}
