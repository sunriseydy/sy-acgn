package dev.sunriseydy.acgn.server.common.repository

import dev.sunriseydy.acgn.common.dto.AdditionalInfo

/**
 * @author SunriseYDY
 * @date 2024-07-17 20:40
 */
interface AdditionalInfoRepository {
    suspend fun selectAdditionalInfos(associatedType: String, associatedId: ULong, additionalType: String? = null): List<AdditionalInfo>
    suspend fun saveAdditionalInfo(additionalInfo: AdditionalInfo, associatedId: ULong? = null): String
    suspend fun saveAdditionalInfos(additionalInfos: List<AdditionalInfo>, associatedId: ULong? = null): List<String>
    suspend fun insertAdditionalInfo(additionalInfo: AdditionalInfo): String
    suspend fun updateAdditionalValue(id: String, value: String): String
    suspend fun deleteAdditionalInfo(id: String)
    suspend fun deleteAdditionalInfos(associatedType: String, associatedId: ULong, additionalType: String? = null)
}