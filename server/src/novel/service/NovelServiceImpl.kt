package dev.sunriseydy.acgn.server.novel.service

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelCreateOrUpdateDto
import dev.sunriseydy.acgn.novel.dto.NovelVolume
import dev.sunriseydy.acgn.novel.dto.NovelVolumeCreateOrUpdateDto
import dev.sunriseydy.acgn.novel.enums.NovelAdditionType
import dev.sunriseydy.acgn.novel.enums.NovelAssociatedType
import dev.sunriseydy.acgn.server.anime.tools.BangumiTool
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository
import dev.sunriseydy.acgn.server.common.service.AttachFileInfoService
import dev.sunriseydy.acgn.server.novel.repository.NovelRepository
import dev.sunriseydy.acgn.server.novel.tools.NovelCacheTool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 轻小说 Service 实现类
 */
class NovelServiceImpl(
    private val novelRepository: NovelRepository,
    private val additionalInfoRepository: AdditionalInfoRepository,
    private val bangumiTool: BangumiTool,
    private val attachFileInfoService: AttachFileInfoService,
) : NovelService {

    private val logger = KotlinLogging.logger { }

    override suspend fun getNovelList(
        fromDb: Boolean,
        name: String?,
        status: String?
    ): List<Novel> = coroutineScope {
        if (fromDb || NovelCacheTool.isNovelEmpty()) {
            val novels = novelRepository.selectAllNovel(name, status)
            novels.map { novel ->
                async {
                    val fullNovel = attachFullNovelDetails(novel)
                    NovelCacheTool.setNovel(fullNovel)
                }
            }.awaitAll()
        } else {
            var cached = NovelCacheTool.getNovelList()
            if (!name.isNullOrBlank()) {
                cached = cached.filter {
                    it.name.contains(name, ignoreCase = true) ||
                            (it.originalName?.contains(name, ignoreCase = true) == true)
                }
            }
            if (!status.isNullOrBlank()) {
                cached = cached.filter { novel ->
                    novel.status.equals(status, ignoreCase = true)
                }
            }
            return@coroutineScope cached
        }
    }

    override suspend fun getNovelById(id: ULong): Novel {
        val cached = NovelCacheTool.getNovelById(id)
        if (cached != null) return cached

        val novel = novelRepository.selectNovelById(id)
        val fullNovel = attachFullNovelDetails(novel)
        NovelCacheTool.setNovel(fullNovel)
        return fullNovel
    }

    private suspend fun attachFullNovelDetails(novel: Novel): Novel = coroutineScope {
        val volumesDeferred = async { getVolumeListByNovelId(novel.id) }
        val additionsDeferred = async {
            additionalInfoRepository.selectAdditionalInfos(
                associatedType = NovelAssociatedType.NOVEL.key,
                associatedId = novel.id
            )
        }
        novel.copy(
            volumes = volumesDeferred.await(),
            additions = additionsDeferred.await()
        )
    }

    override suspend fun createNovel(dto: NovelCreateOrUpdateDto): Novel {
        val novel = Novel(
            id = ULong.MIN_VALUE,
            name = dto.name,
            originalName = dto.originalName,
            author = dto.author,
            illustrator = dto.illustrator,
            description = dto.description,
            publisher = dto.publisher,
            status = dto.status,
            totalVolumes = dto.totalVolumes,
            bgmId = dto.bgmId
        )
        val created = novelRepository.insertNovel(novel)
        val full = attachFullNovelDetails(created)
        NovelCacheTool.setNovel(full)
        return full
    }

    override suspend fun updateNovel(dto: NovelCreateOrUpdateDto): Novel {
        val id = dto.id ?: throw IllegalArgumentException("Novel ID cannot be null for update")
        val existing = novelRepository.selectNovelById(id)
        val updated = existing.copy(
            name = dto.name,
            originalName = dto.originalName ?: existing.originalName,
            author = dto.author ?: existing.author,
            illustrator = dto.illustrator ?: existing.illustrator,
            description = dto.description ?: existing.description,
            publisher = dto.publisher ?: existing.publisher,
            status = dto.status,
            totalVolumes = dto.totalVolumes,
            bgmId = dto.bgmId ?: existing.bgmId
        )
        val result = novelRepository.updateNovel(updated)
        val full = attachFullNovelDetails(result)
        NovelCacheTool.setNovel(full)
        return full
    }

    override suspend fun deleteNovel(id: ULong) {
        additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL.key, id)
        val volumes = novelRepository.selectNovelVolumeByNovelId(id)
        volumes.forEach { volume ->
            deleteVolume(volume.id)
        }
        novelRepository.deleteNovelById(id)
        NovelCacheTool.removeNovel(id)
    }

    override suspend fun getVolumeListByNovelId(novelId: ULong): List<NovelVolume> = coroutineScope {
        val volumes = novelRepository.selectNovelVolumeByNovelId(novelId)
        volumes.map {
            async {
                attachVolumeAdditions(it)
            }
        }.awaitAll()
    }

    override suspend fun getVolumeById(volumeId: ULong): NovelVolume {
        val volume = novelRepository.selectNovelVolumeById(volumeId)
        return attachVolumeAdditions(volume)
    }

    override suspend fun createVolume(dto: NovelVolumeCreateOrUpdateDto): NovelVolume {
        val volume = NovelVolume(
            id = ULong.MIN_VALUE,
            novelId = dto.novelId,
            volumeNumber = dto.volumeNumber,
            name = dto.name,
            description = dto.description,
            releaseDate = dto.releaseDate,
            isbn = dto.isbn,
            bgmId = dto.bgmId
        )
        val inserted = novelRepository.insertNovelVolume(volume)
        dto.readingStatus?.let { status ->
            updateVolumeReadingStatus(inserted.id, status)
        }
        NovelCacheTool.removeNovel(dto.novelId)
        return getVolumeById(inserted.id)
    }

    override suspend fun updateVolume(dto: NovelVolumeCreateOrUpdateDto): NovelVolume {
        val id = dto.id ?: throw IllegalArgumentException("Volume ID cannot be null for update")
        val existing = novelRepository.selectNovelVolumeById(id)
        val updated = existing.copy(
            novelId = dto.novelId,
            volumeNumber = dto.volumeNumber,
            name = dto.name,
            description = dto.description ?: existing.description,
            releaseDate = dto.releaseDate ?: existing.releaseDate,
            isbn = dto.isbn ?: existing.isbn,
            bgmId = dto.bgmId ?: existing.bgmId
        )
        val result = novelRepository.updateNovelVolume(updated)
        dto.readingStatus?.let { status ->
            updateVolumeReadingStatus(result.id, status)
        }
        NovelCacheTool.removeNovel(dto.novelId)
        return getVolumeById(result.id)
    }

    override suspend fun updateVolumeReadingStatus(volumeId: ULong, readingStatus: String): NovelVolume {
        val volume = novelRepository.selectNovelVolumeById(volumeId)
        val oldReadingStatus = additionalInfoRepository.selectAdditionalInfos(
            NovelAssociatedType.NOVEL_VOLUME.key,
            volumeId, NovelAdditionType.ReadingStatus.key
        ).getOrNull(0)
        if (oldReadingStatus != null) {
            additionalInfoRepository.saveAdditionalInfo(oldReadingStatus.copy(additionalValue = readingStatus), volumeId)
        } else {
            val info = AdditionalInfo(
                id = "",
                associatedId = volumeId,
                associatedType = NovelAssociatedType.NOVEL_VOLUME.key,
                additionalType = NovelAdditionType.ReadingStatus.key,
                additionalValue = readingStatus
            )
            additionalInfoRepository.saveAdditionalInfo(info, volumeId)
        }
        NovelCacheTool.removeNovel(volume.novelId)
        return getVolumeById(volumeId)
    }

    override suspend fun deleteVolume(volumeId: ULong) {
        val volume = novelRepository.selectNovelVolumeById(volumeId)
        additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL_VOLUME.key, volumeId)
        novelRepository.deleteNovelVolumeById(volumeId)
        NovelCacheTool.removeNovel(volume.novelId)
    }

    override suspend fun searchBangumiNovel(query: String): List<Novel> {
        return bangumiTool.searchNovel(query)
    }

    override suspend fun importNovelFromBangumi(bgmId: ULong, isUpdate: Boolean): Novel = coroutineScope {
        val existing = novelRepository.selectNovelByBgmId(bgmId)
        if (existing != null && !isUpdate) {
            return@coroutineScope getNovelById(existing.id)
        }

        val bgmNovel = bangumiTool.getNovelSubject(bgmId.toInt())
        val novelId = if (existing != null) {
            val updated = existing.copy(
                name = bgmNovel.name,
                originalName = bgmNovel.originalName ?: existing.originalName,
                author = bgmNovel.author ?: existing.author,
                illustrator = bgmNovel.illustrator ?: existing.illustrator,
                description = bgmNovel.description ?: existing.description,
                publisher = bgmNovel.publisher ?: existing.publisher,
                totalVolumes = bgmNovel.totalVolumes,
                bgmId = bgmId
            )
            novelRepository.updateNovel(updated)
            if (bgmNovel.additions.isNotEmpty()) {
                additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL.key, existing.id, NovelAdditionType.BgmJson.key)
                additionalInfoRepository.saveAdditionalInfos(bgmNovel.additions, existing.id)
            }
            existing.id
        } else {
            val created = novelRepository.insertNovel(bgmNovel)
            if (bgmNovel.additions.isNotEmpty()) {
                additionalInfoRepository.saveAdditionalInfos(bgmNovel.additions, created.id)
            }
            created.id
        }

        val currentNovelAdditions = additionalInfoRepository.selectAdditionalInfos(
            associatedType = NovelAssociatedType.NOVEL.key,
            associatedId = novelId
        )

        async {
            val novelPosterAddition = downloadAndSavePoster(
                associatedId = novelId,
                associatedType = NovelAssociatedType.NOVEL.key,
                additions = currentNovelAdditions,
                forceUpdate = isUpdate
            )
            if (novelPosterAddition != null) {
                additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL.key, novelId, NovelAdditionType.PosterId.key)
                additionalInfoRepository.saveAdditionalInfo(novelPosterAddition, novelId)
            }
        }

        async {
            val bgmVolumes = bangumiTool.getNovelVolumes(bgmId.toInt(), novelId)
            val existingVolumes = novelRepository.selectNovelVolumeByNovelId(novelId)
            val existingVolumeMapByBgmId = existingVolumes.mapNotNull { vol -> vol.bgmId?.let { it to vol } }.toMap()
            val existingVolumeMapByNumber = existingVolumes.associateBy { it.volumeNumber }
            bgmVolumes.map { volume ->
                async {
                    val existingVolume = (volume.bgmId?.let { existingVolumeMapByBgmId[it] })
                        ?: existingVolumeMapByNumber[volume.volumeNumber]

                    val volumeId = if (existingVolume != null) {
                        val updatedVol = existingVolume.copy(
                            novelId = novelId,
                            volumeNumber = volume.volumeNumber,
                            name = volume.name,
                            description = volume.description ?: existingVolume.description,
                            releaseDate = volume.releaseDate ?: existingVolume.releaseDate,
                            isbn = volume.isbn ?: existingVolume.isbn,
                            bgmId = volume.bgmId ?: existingVolume.bgmId
                        )
                        val updatedResult = novelRepository.updateNovelVolume(updatedVol)
                        if (volume.additions.isNotEmpty()) {
                            additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL_VOLUME.key, updatedResult.id, NovelAdditionType.BgmJson.key)
                            additionalInfoRepository.saveAdditionalInfos(volume.additions, updatedResult.id)
                        }
                        updatedResult.id
                    } else {
                        val insertedVolume = novelRepository.insertNovelVolume(volume.copy(novelId = novelId))
                        if (volume.additions.isNotEmpty()) {
                            additionalInfoRepository.saveAdditionalInfos(volume.additions, insertedVolume.id)
                        }
                        insertedVolume.id
                    }

                    val currentVolAdditions = additionalInfoRepository.selectAdditionalInfos(
                        associatedType = NovelAssociatedType.NOVEL_VOLUME.key,
                        associatedId = volumeId
                    )
                    val volPosterAddition = downloadAndSavePoster(
                        associatedId = volumeId,
                        associatedType = NovelAssociatedType.NOVEL_VOLUME.key,
                        additions = currentVolAdditions,
                        forceUpdate = isUpdate
                    )
                    if (volPosterAddition != null) {
                        additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL_VOLUME.key, volumeId, NovelAdditionType.PosterId.key)
                        additionalInfoRepository.saveAdditionalInfo(volPosterAddition, volumeId)
                    }
                }
            }
        }
        getNovelById(novelId)
    }

    private suspend fun downloadAndSavePoster(
        associatedId: ULong,
        associatedType: String,
        additions: List<AdditionalInfo>,
        forceUpdate: Boolean = false
    ): AdditionalInfo? {
        val bgmJson = NovelAdditionType.BgmJson.valueOf(additions) ?: return null
        val imageUrl = bgmJson["images"]?.jsonObject?.get("common")?.jsonPrimitive?.contentOrNull
        if (imageUrl.isNullOrBlank()) return null

        val existingPosterId = NovelAdditionType.PosterId.valueOf(additions)
        if (!existingPosterId.isNullOrBlank() && !forceUpdate) {
            return null
        }

        try {
            val attachFileId = attachFileInfoService.saveFile(
                downloadUrl = imageUrl,
                defaultContentType = "image/jpeg",
                defaultFileName = imageUrl.substringAfterLast("/").ifBlank { "poster.jpg" }
            )
            if (!existingPosterId.isNullOrBlank()) {
                try {
                    attachFileInfoService.deleteFile(existingPosterId)
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to delete old poster attach file $existingPosterId" }
                }
            }
            return AdditionalInfo(
                id = "",
                associatedId = associatedId,
                associatedType = associatedType,
                additionalType = NovelAdditionType.PosterId.key,
                additionalValue = attachFileId
            )
        } catch (e: Exception) {
            logger.error(e) { "Error downloading poster for $associatedType $associatedId from $imageUrl" }
        }
        return null
    }

    private suspend fun attachNovelAdditions(novel: Novel): Novel {
        val additions = additionalInfoRepository.selectAdditionalInfos(
            associatedType = NovelAssociatedType.NOVEL.key,
            associatedId = novel.id
        )
        return novel.copy(additions = additions)
    }

    private suspend fun attachVolumeAdditions(volume: NovelVolume): NovelVolume {
        val additions = additionalInfoRepository.selectAdditionalInfos(
            associatedType = NovelAssociatedType.NOVEL_VOLUME.key,
            associatedId = volume.id
        )
        return volume.copy(additions = additions)
    }
}
