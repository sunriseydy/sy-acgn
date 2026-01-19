package dev.sunriseydy.acgn.server.common.repository

import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.server.common.db.AppConfigDAO
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * @author SunriseYDY
 * @date 2024-07-14 19:38
 */
class AppConfigRepositoryImpl : AppConfigRepository {
    override suspend fun selectAllAppConfig(): List<AppConfig> = suspendTransaction {
        AppConfigDAO.all().map(AppConfigDAO::toDTO)
    }

    override suspend fun insertAppConfig(appConfig: AppConfig): AppConfig = suspendTransaction {
        AppConfigDAO.new {
            this.configKey = appConfig.configKey
            this.configValue = appConfig.configValue
        }.toDTO()
    }

    override suspend fun updateAppConfig(appConfig: AppConfig): AppConfig = suspendTransaction {
        AppConfigDAO.findByIdAndUpdate(appConfig.id) {
            it.configValue = appConfig.configValue
        }?.toDTO() ?: throw NoSuchElementException()
    }

    override suspend fun deleteAppConfig(id: ULong): Unit = suspendTransaction {
        AppConfigDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }
}
