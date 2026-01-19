package dev.sunriseydy.acgn.base
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api")
class ApiResource {
    @Serializable
    @Resource("error")
    class Error(val parent: ApiResource = ApiResource()) {

    }
}