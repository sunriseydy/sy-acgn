package dev.sunriseydy.acgn.server.common.repository

import dev.sunriseydy.acgn.common.dto.AttachFileInfo

/**
 * 附件文件数据访问层接口
 *
 * @author SunriseYDY
 * @date 2026-07-06
 */
interface AttachFileInfoRepository {
    suspend fun selectById(id: String): AttachFileInfo?
    suspend fun insert(attachFileInfo: AttachFileInfo): String
    suspend fun delete(id: String)
}
