package dev.sunriseydy.acgn.server.novel.repository

import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelVolume

/**
 * 轻小说 Repository 接口
 */
interface NovelRepository {
    suspend fun selectAllNovel(name: String? = null, status: String? = null, page: Long = 1, size: Int = 50): List<Novel>
    suspend fun selectNovelById(id: ULong): Novel
    suspend fun selectNovelByBgmId(bgmId: ULong): Novel?
    suspend fun insertNovel(novel: Novel): Novel
    suspend fun updateNovel(novel: Novel): Novel
    suspend fun deleteNovelById(id: ULong)

    suspend fun selectAllNovelVolumes(): List<NovelVolume>
    suspend fun selectNovelVolumeById(id: ULong): NovelVolume
    suspend fun selectNovelVolumeByNovelId(novelId: ULong): List<NovelVolume>
    suspend fun selectNovelVolumeByBgmId(bgmId: ULong): NovelVolume?
    suspend fun insertNovelVolume(volume: NovelVolume): NovelVolume
    suspend fun updateNovelVolume(volume: NovelVolume): NovelVolume
    suspend fun deleteNovelVolumeById(id: ULong)
    suspend fun deleteNovelVolumeByNovelId(novelId: ULong)
}
