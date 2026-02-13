package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.anime.repository.AnimeRepository
import dev.sunriseydy.acgn.server.anime.repository.AnimeRepositoryImpl
import dev.sunriseydy.acgn.server.anime.service.AnimeService
import dev.sunriseydy.acgn.server.anime.service.AnimeServiceImpl
import dev.sunriseydy.acgn.server.anime.tools.BangumiTool
import dev.sunriseydy.acgn.server.anime.tools.QbTool
import dev.sunriseydy.acgn.server.anime.tools.TmdbTool
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepositoryImpl
import dev.sunriseydy.acgn.server.common.repository.AppConfigRepository
import dev.sunriseydy.acgn.server.common.repository.AppConfigRepositoryImpl
import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.server.common.service.AppConfigServiceImpl
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

/**
 * Configure dependency injection for the application
 * @author SunriseYDY
 * @date 2026-01-22
 */
fun Application.configureDependencyInjection() {
    dependencies {
        // Register repositories
        provide<AppConfigRepository> { AppConfigRepositoryImpl() }
        provide<AdditionalInfoRepository> { AdditionalInfoRepositoryImpl() }
        provide<AnimeRepository> { AnimeRepositoryImpl() }

        // Register services
        provide<AppConfigService> {
            AppConfigServiceImpl(resolve<AppConfigRepository>())
        }
        provide<AnimeService> {
            AnimeServiceImpl(
                resolve<AnimeRepository>(),
                resolve<AdditionalInfoRepository>()
            )
        }
        provide<BangumiTool> { BangumiTool() }
        provide<QbTool> { QbTool() }
        provide<TmdbTool> { TmdbTool() }
    }
}
