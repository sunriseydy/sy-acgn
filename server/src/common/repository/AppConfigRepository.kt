package dev.sunriseydy.acgn.server.common.repository

import dev.sunriseydy.acgn.common.dto.AppConfig

/**
 * @author SunriseYDY
 * @date 2024-07-14 19:38
 */
interface AppConfigRepository {
    suspend fun selectAllAppConfig(): List<AppConfig>
    suspend fun insertAppConfig(appConfig: AppConfig): AppConfig
    suspend fun updateAppConfig(appConfig: AppConfig): AppConfig
    suspend fun deleteAppConfig(id: ULong)
}