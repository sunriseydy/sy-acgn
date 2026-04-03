package dev.sunriseydy.acgn.server.common.db

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.common.dto.AppConfig
import kotlin.time.Instant as KtInstant
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.dao.ULongEntity
import org.jetbrains.exposed.v1.dao.ULongEntityClass
import org.jetbrains.exposed.v1.dao.java.UUIDEntity
import org.jetbrains.exposed.v1.dao.java.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.util.*

/**
 * @author SunriseYDY
 * @date 2024-07-14 18:56
 */
object AdditionalInfoTable : UUIDTable("common_addition") {
    val associatedId = ulong("associated_id")
    val associatedType = varchar("associated_type", 256)
    val additionalType = varchar("additional_type", 256)
    val additionalValue = text("additional_value", eagerLoading = true)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        uniqueIndex(associatedType, associatedId, additionalType)
    }
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
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

object AppConfigTable : ULongIdTable("common_config") {
    val configKey = varchar("config_key", 256)
    val configValue = text("config_value", eagerLoading = true)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

class AppConfigDAO(id: EntityID<ULong>) : ULongEntity(id) {
    companion object : ULongEntityClass<AppConfigDAO>(AppConfigTable)

    var configKey by AppConfigTable.configKey
    var configValue by AppConfigTable.configValue
    var createdAt by AppConfigTable.createdAt
    var updatedAt by AppConfigTable.updatedAt

    fun toDTO() = AppConfig(
        id = id.value,
        configKey = configKey,
        configValue = configValue,
        createdAt = createdAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
        updatedAt = updatedAt.toInstant().let { KtInstant.fromEpochSeconds(it.epochSecond, it.nano) },
    )
}

fun commonModuleTables() = listOf(AdditionalInfoTable, AppConfigTable)
