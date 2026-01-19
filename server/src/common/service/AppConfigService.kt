package dev.sunriseydy.acgn.server.common.service

import dev.sunriseydy.acgn.common.dto.AppConfig

/**
 * @author SunriseYDY
 * @date 2024-07-14 20:54
 */
interface AppConfigService {
    suspend fun getAllAppConfigFromDB(): List<AppConfig>
    suspend fun saveAppConfig(appConfig: AppConfig): AppConfig
    suspend fun saveAppConfigs(appConfigs: List<AppConfig>): List<AppConfig>
    suspend fun deleteAppConfig(appConfig: AppConfig)
}