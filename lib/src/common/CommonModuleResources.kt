package dev.sunriseydy.acgn.common

import dev.sunriseydy.acgn.base.ApiResource
import dev.sunriseydy.acgn.base.enums.Language
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("common")
class CommonModuleResource(val parent: ApiResource = ApiResource()) {

    @Serializable
    @Resource("info")
    class Info(val parent: CommonModuleResource = CommonModuleResource(), val language: Language? = null)

    @Serializable
    @Resource("localization")
    class Localization(val parent: CommonModuleResource = CommonModuleResource())

    @Serializable
    @Resource("config")
    class Config(val parent: CommonModuleResource = CommonModuleResource()) {
        @Serializable
        @Resource("map")
        class Map(val parent: Config = Config())
    }

    @Serializable
    @Resource("addition")
    class Addition(
        val parent: CommonModuleResource = CommonModuleResource(),
        val associatedType: String? = null,
        val associatedId: ULong? = null,
        val additionalType: String? = null,
        val id: String? = null
    )

    @Serializable
    @Resource("attach/{id}")
    class AttachFile(val parent: CommonModuleResource = CommonModuleResource(), val id: String)
}
