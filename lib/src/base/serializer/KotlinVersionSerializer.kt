package dev.sunriseydy.acgn.base.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * KotlinVersion 序列化器
 *
 * 将 [KotlinVersion] 序列化为 IntArray [major, minor, patch]。
 */
object KotlinVersionSerializer : KSerializer<KotlinVersion> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("kotlin.KotlinVersion", IntArraySerializer().descriptor)

    override fun serialize(encoder: Encoder, value: KotlinVersion) {
        encoder.encodeSerializableValue(IntArraySerializer(), intArrayOf(value.major, value.minor, value.patch))
    }

    override fun deserialize(decoder: Decoder): KotlinVersion {
        val array = decoder.decodeSerializableValue(IntArraySerializer())
        return KotlinVersion(array[0], array[1], array[2])
    }
}