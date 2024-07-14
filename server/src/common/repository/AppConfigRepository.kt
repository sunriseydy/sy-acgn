package dev.sunriseydy.acgn.common.repository

import dev.sunriseydy.acgn.common.db.AppConfigDAO
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.plugins.suspendTransaction

/**
 * @author SunriseYDY
 * @date 2024-07-14 19:38
 */
class AppConfigRepository {
    suspend fun selectAllAppConfig(): List<AppConfig> = suspendTransaction {
        AppConfigDAO.all().map(AppConfigDAO::toDTO)
    }

    suspend fun insertAppConfig(appConfig: AppConfig): AppConfig = suspendTransaction {
        AppConfigDAO.new {
            this.configKey = appConfig.configKey
            this.configValue = appConfig.configValue
        }.toDTO()
    }

    suspend fun updateAppConfig(appConfig: AppConfig): AppConfig = suspendTransaction {
        AppConfigDAO.findByIdAndUpdate(appConfig.id) {
            it.configValue = appConfig.configValue
        }?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun deleteAppConfig(id: ULong): Unit = suspendTransaction {
        AppConfigDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }
}