package dev.sunriseydy.acgn.game

import dev.sunriseydy.acgn.base.ApiResource
import io.ktor.resources.*
import kotlinx.serialization.Serializable

/**
 * Game 模块 API 路由资源定义
 *
 * 使用 Ktor Resources 插件实现类型安全路由。
 * 基础路径: `/api/game`
 */
@Serializable
@Resource("game")
class GameModuleResource(val parent: ApiResource = ApiResource()) {

    /** 游戏数据管理 API: `/api/game/game` */
    @Serializable
    @Resource("game")
    class Game(val parent: GameModuleResource = GameModuleResource()) {

        @Serializable
        @Resource("list")
        class List(
            val parent: Game = Game(),
            val name: String? = null,
            val platform: String? = null,
            val playStatus: String? = null,
            val page: Long = 1,
            val size: Int = 50
        )

        @Serializable
        @Resource("{id}")
        class Id(val parent: Game = Game(), val id: ULong)

        @Serializable
        @Resource("{id}/play-record")
        class PlayRecord(val parent: Game = Game(), val id: ULong)

        @Serializable
        @Resource("{id}/release")
        class Release(val parent: Game = Game(), val id: ULong) {
            @Serializable
            @Resource("{releaseId}")
            class Id(val parent: Release = Release(id = 0uL), val releaseId: ULong)
        }

        /** Bangumi 集成 API: `/api/game/game/bangumi` */
        @Serializable
        @Resource("bangumi")
        class Bangumi(val parent: Game = Game()) {

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

        /** Steam 集成 API: `/api/game/game/steam` */
        @Serializable
        @Resource("steam")
        class Steam(val parent: Game = Game()) {

            @Serializable
            @Resource("search")
            class Search(val parent: Steam = Steam(), val query: String)

            @Serializable
            @Resource("app-detail")
            class AppDetail(val parent: Steam = Steam(), val appId: ULong)

            @Serializable
            @Resource("import/{appId}")
            class Import(val parent: Steam = Steam(), val appId: ULong, val isUpdate: Boolean = false)
        }
    }
}
