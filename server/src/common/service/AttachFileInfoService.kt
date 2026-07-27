package dev.sunriseydy.acgn.server.common.service

import dev.sunriseydy.acgn.common.dto.AttachFileInfo
import java.io.InputStream

/**
 * 附件文件业务逻辑层接口
 *
 * @author SunriseYDY
 * @date 2026-07-06
 */
interface AttachFileInfoService {
    suspend fun getFileById(id: String): AttachFileInfo?
    suspend fun getFileStream(id: String): InputStream
    suspend fun saveFile(fileName: String, inputStream: InputStream, contentLength: Long, contentType: String): String
    suspend fun saveFile(downloadUrl: String, defaultContentType: String, defaultFileName: String): String
    suspend fun deleteFile(id: String)
}
