package dev.sunriseydy.acgn.server.novel.repository

import dev.sunriseydy.acgn.novel.dto.Novel
import dev.sunriseydy.acgn.novel.dto.NovelVolume
import dev.sunriseydy.acgn.server.base.plugins.paging
import dev.sunriseydy.acgn.server.novel.db.*
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * 轻小说数据仓储实现类
 */
class NovelRepositoryImpl : NovelRepository {
    override suspend fun selectAllNovel(
        name: String?,
        status: String?,
        page: Long,
        size: Int
    ): List<Novel> = suspendTransaction {
        NovelDAO.find {
            (name?.let { NovelTable.name like "%$it%" } ?: Op.TRUE) and
                    (status?.let { NovelTable.status eq it } ?: Op.TRUE)
        }.orderBy(NovelTable.createdAt to SortOrder.DESC)
            .paging(page, size)
            .map(NovelDAO::toDTO)
    }

    override suspend fun selectNovelById(id: ULong): Novel = suspendTransaction {
        NovelDAO.findById(id)?.toDTO() ?: throw NoSuchElementException("Novel with id $id not found")
    }

    override suspend fun selectNovelByBgmId(bgmId: ULong): Novel? = suspendTransaction {
        NovelDAO.find { NovelTable.bgmId eq bgmId }.firstOrNull()?.toDTO()
    }

    override suspend fun insertNovel(novel: Novel): Novel = suspendTransaction {
        NovelDAO.new {
            this.name = novel.name
            this.originalName = novel.originalName
            this.author = novel.author
            this.illustrator = novel.illustrator
            this.description = novel.description
            this.publisher = novel.publisher
            this.status = novel.status
            this.totalVolumes = novel.totalVolumes
            this.bgmId = novel.bgmId
        }.toDTO()
    }

    override suspend fun updateNovel(novel: Novel): Novel = suspendTransaction {
        NovelDAO.findByIdAndUpdate(novel.id) {
            it.name = novel.name
            it.originalName = novel.originalName
            it.author = novel.author
            it.illustrator = novel.illustrator
            it.description = novel.description
            it.publisher = novel.publisher
            it.status = novel.status
            it.totalVolumes = novel.totalVolumes
            novel.bgmId?.apply { it.bgmId = novel.bgmId }
        }?.toDTO() ?: throw NoSuchElementException("Novel with id ${novel.id} not found")
    }

    override suspend fun deleteNovelById(id: ULong): Unit = suspendTransaction {
        NovelDAO.findById(id)?.delete() ?: throw NoSuchElementException("Novel with id $id not found")
    }

    override suspend fun selectAllNovelVolumes(): List<NovelVolume> = suspendTransaction {
        NovelVolumeDAO.all().map(NovelVolumeDAO::toDTO)
    }

    override suspend fun selectNovelVolumeById(id: ULong): NovelVolume = suspendTransaction {
        NovelVolumeDAO.findById(id)?.toDTO() ?: throw NoSuchElementException("Novel volume with id $id not found")
    }

    override suspend fun selectNovelVolumeByNovelId(novelId: ULong): List<NovelVolume> = suspendTransaction {
        NovelVolumeDAO.find {
            NovelVolumeTable.novelId eq novelId
        }.orderBy(NovelVolumeTable.volumeNumber to SortOrder.ASC).map(NovelVolumeDAO::toDTO)
    }

    override suspend fun selectNovelVolumeByBgmId(bgmId: ULong): NovelVolume? = suspendTransaction {
        NovelVolumeDAO.find { NovelVolumeTable.bgmId eq bgmId }.firstOrNull()?.toDTO()
    }

    override suspend fun insertNovelVolume(volume: NovelVolume): NovelVolume = suspendTransaction {
        NovelVolumeDAO.new {
            this.novelId = volume.novelId
            this.volumeNumber = volume.volumeNumber
            this.name = volume.name
            this.description = volume.description
            this.releaseDate = volume.releaseDate
            this.isbn = volume.isbn
            this.bgmId = volume.bgmId
        }.toDTO()
    }

    override suspend fun updateNovelVolume(volume: NovelVolume): NovelVolume = suspendTransaction {
        NovelVolumeDAO.findByIdAndUpdate(volume.id) {
            it.novelId = volume.novelId
            it.volumeNumber = volume.volumeNumber
            it.name = volume.name
            it.description = volume.description
            it.releaseDate = volume.releaseDate
            it.isbn = volume.isbn
            volume.bgmId?.apply { it.bgmId = volume.bgmId }
        }?.toDTO() ?: throw NoSuchElementException("Novel volume with id ${volume.id} not found")
    }

    override suspend fun deleteNovelVolumeById(id: ULong): Unit = suspendTransaction {
        NovelVolumeDAO.findById(id)?.delete() ?: throw NoSuchElementException("Novel volume with id $id not found")
    }

    override suspend fun deleteNovelVolumeByNovelId(novelId: ULong): Unit = suspendTransaction {
        NovelVolumeDAO.find {
            NovelVolumeTable.novelId eq novelId
        }.forEach { it.delete() }
    }
}
