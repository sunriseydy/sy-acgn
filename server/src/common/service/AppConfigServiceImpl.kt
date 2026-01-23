package dev.sunriseydy.acgn.server.common.service

import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.server.common.repository.AppConfigRepository

/**
 * @author SunriseYDY
 * @date 2024-07-14 20:54
 */
class AppConfigServiceImpl(val appConfigRepository: AppConfigRepository) : AppConfigService {
    override suspend fun getAllAppConfigFromDB(): List<AppConfig> =
        appConfigRepository.selectAllAppConfig()

    override suspend fun saveAppConfig(appConfig: AppConfig): AppConfig =
        if (appConfig.id == 0.toULong()) {
            appConfigRepository.insertAppConfig(appConfig)
        } else {
            appConfigRepository.updateAppConfig(appConfig)
        }

    override suspend fun saveAppConfigs(appConfigs: List<AppConfig>): List<AppConfig> =
        appConfigs.map {
            saveAppConfig(it)
        }

    override suspend fun deleteAppConfig(appConfig: AppConfig) =
        appConfigRepository.deleteAppConfig(appConfig.id)
}
