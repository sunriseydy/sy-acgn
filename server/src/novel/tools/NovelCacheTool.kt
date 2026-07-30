package dev.sunriseydy.acgn.server.novel.tools

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.sunriseydy.acgn.novel.dto.Novel

/**
 * 轻小说数据缓存工具
 */
object NovelCacheTool {
    private val novelByIdCache: Cache<ULong, Novel> = Caffeine.newBuilder().build()

    fun isNovelEmpty() = novelByIdCache.asMap().isEmpty()

    fun refreshNovelCache(novelList: List<Novel>) {
        novelByIdCache.invalidateAll()
        novelList.forEach { setNovel(it) }
    }

    fun setNovel(novel: Novel): Novel {
        novelByIdCache.put(novel.id, novel)
        return novel
    }

    fun getNovelList(): List<Novel> = novelByIdCache.asMap().values.toList()

    fun getNovelById(id: ULong): Novel? = novelByIdCache.getIfPresent(id)

    fun removeNovel(id: ULong) {
        novelByIdCache.invalidate(id)
    }
}
