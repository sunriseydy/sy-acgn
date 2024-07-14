package dev.sunriseydy.acgn.common.db

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.UUID

/**
 * @author SunriseYDY
 * @date 2024-07-14 18:56
 */
object AdditionalInfoTable : UUIDTable("common_addition") {
    val associatedId = ulong("associated_id")
    val associatedType = varchar("associated_type", 256)
    val additionalType = varchar("additional_type", 256)
    val additionalValue = text("additional_value", eagerLoading = true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

class AdditionalInfoDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AdditionalInfoDAO>(AdditionalInfoTable)

    var associatedId by AdditionalInfoTable.associatedId
    var associatedType by AdditionalInfoTable.associatedType
    var additionalType by AdditionalInfoTable.additionalType
    var additionalValue by AdditionalInfoTable.additionalValue
    var createdAt by AdditionalInfoTable.createdAt
    var updatedAt by AdditionalInfoTable.updatedAt

    fun toDTO() = AdditionalInfo(
        id = id.toString(),
        associatedId = associatedId,
        associatedType = associatedType,
        additionalType = additionalType,
        additionalValue = additionalValue,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun commonModuleTables() = listOf(AdditionalInfoTable)
