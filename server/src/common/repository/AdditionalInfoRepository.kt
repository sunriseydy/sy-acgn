package dev.sunriseydy.acgn.common.repository

import dev.sunriseydy.acgn.common.db.AdditionalInfoDAO
import dev.sunriseydy.acgn.common.db.AdditionalInfoTable
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.plugins.suspendTransaction
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.util.UUID

/**
 * @author SunriseYDY
 * @date 2024-07-17 20:40
 */
class AdditionalInfoRepository {
    suspend fun selectAdditionalInfos(associatedType: String, associatedId: ULong, additionalType: String? = null) =
        suspendTransaction {
            AdditionalInfoDAO.find {
                (AdditionalInfoTable.associatedType eq associatedType) and
                        (AdditionalInfoTable.associatedId eq associatedId) and
                        (additionalType?.let { AdditionalInfoTable.additionalType eq additionalType } ?: Op.TRUE)
            }.map(AdditionalInfoDAO::toDTO)
        }

    suspend fun saveAdditionalInfo(additionalInfo: AdditionalInfo, associatedId: ULong? = null) =
        if (additionalInfo.id.isEmpty()) {
            this.insertAdditionalInfo(associatedId?.let { additionalInfo.copy(associatedId = it) } ?: additionalInfo)
        } else {
            this.updateAdditionalInfo(associatedId?.let { additionalInfo.copy(associatedId = it) } ?: additionalInfo)
        }

    suspend fun saveAdditionalInfos(additionalInfos: List<AdditionalInfo>, associatedId: ULong? = null) =
        additionalInfos.map { this.saveAdditionalInfo(it, associatedId) }

    suspend fun insertAdditionalInfo(additionalInfo: AdditionalInfo) = suspendTransaction {
        check(additionalInfo.associatedId != ULong.MIN_VALUE) { "associatedId 为 0" }
        AdditionalInfoDAO.new {
            this.associatedId = additionalInfo.associatedId
            this.associatedType = additionalInfo.associatedType
            this.additionalType = additionalInfo.additionalType
            this.additionalValue = additionalInfo.additionalValue
        }.id.toString()
    }

    suspend fun updateAdditionalInfo(additionalInfo: AdditionalInfo) = suspendTransaction {
        check(additionalInfo.associatedId != ULong.MIN_VALUE) { "associatedId 为 0" }
        AdditionalInfoDAO.findByIdAndUpdate(UUID.fromString(additionalInfo.id)) {
            it.additionalValue = additionalInfo.additionalValue
        }?.id?.toString() ?: throw NoSuchElementException()
    }

    suspend fun deleteAdditionalInfo(id: String) = suspendTransaction {
        AdditionalInfoDAO.findById(UUID.fromString(id))?.delete() ?: throw NoSuchElementException()
    }

    suspend fun deleteAdditionalInfos(associatedType: String, associatedId: ULong, additionalType: String? = null) =
        suspendTransaction {
            AdditionalInfoTable.deleteWhere {
                (AdditionalInfoTable.associatedType eq associatedType) and
                        (AdditionalInfoTable.associatedId eq associatedId) and
                        (additionalType?.let { AdditionalInfoTable.additionalType eq additionalType } ?: Op.TRUE)
            }
        }
}