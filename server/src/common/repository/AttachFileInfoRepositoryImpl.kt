package dev.sunriseydy.acgn.server.common.repository

import dev.sunriseydy.acgn.common.dto.AttachFileInfo
import dev.sunriseydy.acgn.server.common.db.AttachFileInfoDAO
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

/**
 * 附件文件数据访问层实现类
 *
 * @author SunriseYDY
 * @date 2026-07-06
 */
class AttachFileInfoRepositoryImpl : AttachFileInfoRepository {
    override suspend fun selectById(id: String): AttachFileInfo? = suspendTransaction {
        AttachFileInfoDAO.findById(UUID.fromString(id))?.toDTO()
    }

    override suspend fun insert(attachFileInfo: AttachFileInfo): String = suspendTransaction {
        AttachFileInfoDAO.new(UUID.fromString(attachFileInfo.id)) {
            this.fileName = attachFileInfo.fileName
            this.fileKey = attachFileInfo.fileKey
            this.contentType = attachFileInfo.contentType
            this.fileSize = attachFileInfo.fileSize
        }.id.toString()
    }

    override suspend fun delete(id: String): Unit = suspendTransaction {
        AttachFileInfoDAO.findById(UUID.fromString(id))?.delete() ?: throw NoSuchElementException("Attach file not found: $id")
    }
}
