package dev.sunriseydy.acgn.server.common.repository

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.server.common.db.AdditionalInfoDAO
import dev.sunriseydy.acgn.server.common.db.AdditionalInfoTable
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

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
            this.updateAdditionalValue(additionalInfo.id, additionalInfo.additionalValue)
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

    suspend fun updateAdditionalValue(id: String, value: String) = suspendTransaction {
        AdditionalInfoDAO.findByIdAndUpdate(UUID.fromString(id)) {
            it.additionalValue = value
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