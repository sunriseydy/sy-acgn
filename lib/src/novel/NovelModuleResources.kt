package dev.sunriseydy.acgn.novel

import dev.sunriseydy.acgn.base.ApiResource
import io.ktor.resources.*
import kotlinx.serialization.Serializable

/**
 * Novel 模块 API 路由资源定义
 *
 * 使用 Ktor Resources 插件实现类型安全路由。
 * 基础路径: `/api/novel`
 */
@Serializable
@Resource("novel")
class NovelModuleResource(val parent: ApiResource = ApiResource()) {

    /** 小说数据管理 API: `/api/novel/novel` */
    @Serializable
    @Resource("novel")
    class Novel(val parent: NovelModuleResource = NovelModuleResource()) {

        @Serializable
        @Resource("list")
        class List(
            val parent: Novel = Novel(),
            val fromDb: Boolean = true,
            val name: String? = null,
            val status: String? = null
        )

        @Serializable
        @Resource("{id}")
        class Id(val parent: Novel = Novel(), val id: ULong)

        /** 卷管理 API: `/api/novel/novel/volume` */
        @Serializable
        @Resource("volume")
        class Volume(val parent: Novel = Novel()) {

            @Serializable
            @Resource("{volumeId}")
            class Id(val parent: Volume = Volume(), val volumeId: ULong)

            @Serializable
            @Resource("{volumeId}/reading-status")
            class ReadingStatus(val parent: Volume = Volume(), val volumeId: ULong)
        }

        /** Bangumi 集成 API: `/api/novel/novel/bangumi` */
        @Serializable
        @Resource("bangumi")
        class Bangumi(val parent: Novel = Novel()) {

            @Serializable
            @Resource("search")
            class Search(val parent: Bangumi = Bangumi(), val query: String)

            @Serializable
            @Resource("subject-detail")
            class SubjectDetail(val parent: Bangumi = Bangumi(), val id: ULong)

            @Serializable
            @Resource("import/{bgmId}")
            class Import(val parent: Bangumi = Bangumi(), val bgmId: ULong, val isUpdate: Boolean = false)
        }
    }
}
