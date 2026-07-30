package dev.sunriseydy.acgn.server.novel.service

import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelCreateOrUpdateDto
import dev.sunriseydy.acgn.novel.dto.NovelVolume
import dev.sunriseydy.acgn.novel.dto.NovelVolumeCreateOrUpdateDto

/**
 * 轻小说 Service 接口
 */
interface NovelService {
    suspend fun getNovelList(fromDb: Boolean = true, name: String? = null, status: String? = null): List<Novel>
    suspend fun getNovelById(id: ULong): Novel
    suspend fun createNovel(dto: NovelCreateOrUpdateDto): Novel
    suspend fun updateNovel(dto: NovelCreateOrUpdateDto): Novel
    suspend fun deleteNovel(id: ULong)

    suspend fun getVolumeListByNovelId(novelId: ULong): List<NovelVolume>
    suspend fun getVolumeById(volumeId: ULong): NovelVolume
    suspend fun createVolume(dto: NovelVolumeCreateOrUpdateDto): NovelVolume
    suspend fun updateVolume(dto: NovelVolumeCreateOrUpdateDto): NovelVolume
    suspend fun updateVolumeReadingStatus(volumeId: ULong, readingStatus: String): NovelVolume
    suspend fun deleteVolume(volumeId: ULong)

    suspend fun searchBangumiNovel(query: String): List<Novel>
    suspend fun importNovelFromBangumi(bgmId: ULong, isUpdate: Boolean = false): Novel
}
