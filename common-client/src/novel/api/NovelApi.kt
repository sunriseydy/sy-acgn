package dev.sunriseydy.acgn.client.novel.api

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.novel.NovelModuleResource
import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelCreateOrUpdateDto
import dev.sunriseydy.acgn.novel.dto.NovelVolume
import dev.sunriseydy.acgn.novel.dto.NovelVolumeCreateOrUpdateDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*

/**
 * 轻小说 API 客户端
 */
class NovelApi internal constructor(private val httpClient: HttpClient) {
    suspend fun getNovelList(name: String? = null, status: String? = null, page: Long = 1, size: Int = 50): Result<List<Novel>> =
        httpClient.get(NovelModuleResource.Novel.List(name = name, status = status, page = page, size = size)).body()

    suspend fun getNovelById(id: ULong): Result<Novel> =
        httpClient.get(NovelModuleResource.Novel.Id(id = id)).body()

    suspend fun createNovel(dto: NovelCreateOrUpdateDto): Result<Novel> =
        httpClient.post(NovelModuleResource.Novel()) { setBody(dto) }.body()

    suspend fun updateNovel(dto: NovelCreateOrUpdateDto): Result<Novel> =
        httpClient.put(NovelModuleResource.Novel()) { setBody(dto) }.body()

    suspend fun deleteNovel(id: ULong): Result<Boolean> =
        httpClient.delete(NovelModuleResource.Novel.Id(id = id)).body()

    suspend fun createVolume(dto: NovelVolumeCreateOrUpdateDto): Result<NovelVolume> =
        httpClient.post(NovelModuleResource.Novel.Volume()) { setBody(dto) }.body()

    suspend fun updateVolume(dto: NovelVolumeCreateOrUpdateDto): Result<NovelVolume> =
        httpClient.put(NovelModuleResource.Novel.Volume()) { setBody(dto) }.body()

    suspend fun deleteVolume(volumeId: ULong): Result<Boolean> =
        httpClient.delete(NovelModuleResource.Novel.Volume.Id(volumeId = volumeId)).body()

    suspend fun updateVolumeReadingStatus(volumeId: ULong, status: String): Result<NovelVolume> =
        httpClient.put(NovelModuleResource.Novel.Volume.ReadingStatus(volumeId = volumeId)) {
            setBody(mapOf("readingStatus" to status))
        }.body()

    suspend fun searchBangumiNovel(query: String): Result<List<Novel>> =
        httpClient.get(NovelModuleResource.Novel.Bangumi.Search(query = query)).body()

    suspend fun importNovelFromBangumi(bgmId: ULong, isUpdate: Boolean = false): Result<Novel> =
        httpClient.post(NovelModuleResource.Novel.Bangumi.Import(bgmId = bgmId, isUpdate = isUpdate)).body()
}
