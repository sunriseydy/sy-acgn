package dev.sunriseydy.acgn.server.common.service

import dev.sunriseydy.acgn.common.dto.AttachFileInfo
import dev.sunriseydy.acgn.server.base.tool.S3Tool
import dev.sunriseydy.acgn.server.common.repository.AttachFileInfoRepository
import java.io.InputStream
import java.util.*

/**
 * 附件文件业务逻辑层实现类
 *
 * @author SunriseYDY
 * @date 2026-07-06
 */
class AttachFileInfoServiceImpl(
    private val repository: AttachFileInfoRepository,
    private val s3Tool: S3Tool
) : AttachFileInfoService {

    override suspend fun getFileById(id: String): AttachFileInfo? {
        return repository.selectById(id)
    }

    override suspend fun getFileStream(id: String): InputStream {
        val fileInfo = repository.selectById(id) ?: throw NoSuchElementException("Attach file not found: $id")
        return s3Tool.getObject(fileInfo.fileKey)
    }

    override suspend fun saveFile(
        fileName: String,
        inputStream: InputStream,
        contentLength: Long,
        contentType: String
    ): String {
        val fileKey = UUID.randomUUID().toString()
        s3Tool.putObject(fileKey, inputStream, contentLength, contentType)
        val attachFileInfo = AttachFileInfo(
            id = fileKey,
            fileName = fileName,
            fileKey = fileKey,
            contentType = contentType,
            fileSize = contentLength
        )
        return repository.insert(attachFileInfo)
    }

    override suspend fun deleteFile(id: String) {
        val fileInfo = repository.selectById(id) ?: throw NoSuchElementException("Attach file not found: $id")
        s3Tool.deleteObject(fileInfo.fileKey)
        repository.delete(id)
    }
}
