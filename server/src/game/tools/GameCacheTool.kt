package dev.sunriseydy.acgn.server.game.tools

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.sunriseydy.acgn.game.dto.Game

/**
 * 游戏数据缓存工具
 */
object GameCacheTool {
    private val gameByIdCache: Cache<ULong, Game> = Caffeine.newBuilder().build()

    fun isGameEmpty() = gameByIdCache.asMap().isEmpty()

    fun refreshGameCache(gameList: List<Game>) {
        gameByIdCache.invalidateAll()
        gameList.forEach { setGame(it) }
    }

    fun setGame(game: Game): Game {
        gameByIdCache.put(game.id, game)
        return game
    }

    fun getGameList(): List<Game> = gameByIdCache.asMap().values.toList()

    fun getGameById(id: ULong): Game? = gameByIdCache.getIfPresent(id)

    fun removeGame(id: ULong) {
        gameByIdCache.invalidate(id)
    }
}
