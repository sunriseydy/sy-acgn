package dev.sunriseydy.acgn.common.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * @author SunriseYDY
 * @date 2024-08-08 23:33
 */
@Serializable
data class AppInfo(
    @Serializable(KotlinVersionSerializer::class) val version: KotlinVersion = KotlinVersion(0, 0, 1),
    val configs: MutableMap<String, Pair<AppConfig?, String?>> = mutableMapOf(),
    val localizations: MutableMap<String, String> = mutableMapOf(),
)

object KotlinVersionSerializer : KSerializer<KotlinVersion> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("kotlin.KotlinVersion", IntArraySerializer().descriptor)
    override fun serialize(
        encoder: Encoder,
        value: KotlinVersion
    ) {
        encoder.encodeSerializableValue(IntArraySerializer(), intArrayOf(value.major, value.minor, value.patch))
    }

    override fun deserialize(decoder: Decoder): KotlinVersion {
        val array = decoder.decodeSerializableValue(IntArraySerializer())
        return KotlinVersion(
            array[0],
            array[1],
            array[2]
        )
    }
}