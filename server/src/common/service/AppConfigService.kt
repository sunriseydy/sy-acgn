package dev.sunriseydy.acgn.common.service

import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.repository.AppConfigRepository
import dev.sunriseydy.acgn.plugins.loadAppConfigFromDB
import dev.sunriseydy.acgn.tools.AppConfigTool

/**
 * @author SunriseYDY
 * @date 2024-07-14 20:54
 */
class AppConfigService(val appConfigRepository: AppConfigRepository = AppConfigRepository()) {
    suspend fun getAllAppConfigFromDB(): List<AppConfig> =
        appConfigRepository.selectAllAppConfig()

    suspend fun saveAppConfig(appConfig: AppConfig): AppConfig =
        if (appConfig.id == 0.toULong()) {
            appConfigRepository.insertAppConfig(appConfig)
        } else {
            appConfigRepository.updateAppConfig(appConfig)
        }

    suspend fun saveAppConfigs(appConfigs: List<AppConfig>): List<AppConfig> =
        appConfigs.map {
            saveAppConfig(it)
        }.also {
            AppConfigTool.loadAppConfigFromDB()
        }

    suspend fun deleteAppConfig(appConfig: AppConfig) =
        appConfigRepository.deleteAppConfig(appConfig.id)
}