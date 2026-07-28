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

    override suspend fun getNovelList(name: String?, status: String?, page: Long, size: Int): List<Novel> {
        val novels = novelRepository.selectAllNovel(name, status, page, size)
        return novels.map { attachNovelAdditions(it) }
    }

    override suspend fun getNovelById(id: ULong): Novel {
        val novel = novelRepository.selectNovelById(id)
        val volumes = getVolumeListByNovelId(id)
        return attachNovelAdditions(novel).copy(volumes = volumes)
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
        return novelRepository.insertNovel(novel)
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
        return attachNovelAdditions(result)
    }

    override suspend fun deleteNovel(id: ULong) {
        additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL.key, id)
        val volumes = novelRepository.selectNovelVolumeByNovelId(id)
        volumes.forEach { volume ->
            deleteVolume(volume.id)
        }
        novelRepository.deleteNovelById(id)
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
        return getVolumeById(result.id)
    }

    override suspend fun updateVolumeReadingStatus(volumeId: ULong, readingStatus: String): NovelVolume {
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
        return getVolumeById(volumeId)
    }

    override suspend fun deleteVolume(volumeId: ULong) {
        additionalInfoRepository.deleteAdditionalInfos(NovelAssociatedType.NOVEL_VOLUME.key, volumeId)
        novelRepository.deleteNovelVolumeById(volumeId)
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
