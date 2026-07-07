package dev.sunriseydy.acgn.server.base.plugins

import dev.sunriseydy.acgn.server.anime.repository.AnimeRepository
import dev.sunriseydy.acgn.server.anime.repository.AnimeRepositoryImpl
import dev.sunriseydy.acgn.server.anime.service.AnimeService
import dev.sunriseydy.acgn.server.anime.service.AnimeServiceImpl
import dev.sunriseydy.acgn.server.anime.tools.BangumiTool
import dev.sunriseydy.acgn.server.anime.tools.QbTool
import dev.sunriseydy.acgn.server.anime.tools.TmdbTool
import dev.sunriseydy.acgn.server.base.tool.S3Tool
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepository
import dev.sunriseydy.acgn.server.common.repository.AdditionalInfoRepositoryImpl
import dev.sunriseydy.acgn.server.common.repository.AppConfigRepository
import dev.sunriseydy.acgn.server.common.repository.AppConfigRepositoryImpl
import dev.sunriseydy.acgn.server.common.repository.AttachFileInfoRepository
import dev.sunriseydy.acgn.server.common.repository.AttachFileInfoRepositoryImpl
import dev.sunriseydy.acgn.server.common.service.AppConfigService
import dev.sunriseydy.acgn.server.common.service.AppConfigServiceImpl
import dev.sunriseydy.acgn.server.common.service.AttachFileInfoService
import dev.sunriseydy.acgn.server.common.service.AttachFileInfoServiceImpl
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

/**
 * 配置应用程序的依赖注入 (DI)
 *
 * 使用 Ktor Server DI 插件注册 Repository、Service 和工具类。
 * 通过 `dependencies { ... }` 块进行定义，实现接口与实现的解耦。
 *
 * @author SunriseYDY
 * @date 2026-01-22
 */
fun Application.configureDependencyInjection() {
    dependencies {
        // --- 注册 Repository (数据访问层) ---
        provide<AppConfigRepository> { AppConfigRepositoryImpl() }
        provide<AdditionalInfoRepository> { AdditionalInfoRepositoryImpl() }
        provide<AnimeRepository> { AnimeRepositoryImpl() }
        provide<AttachFileInfoRepository> { AttachFileInfoRepositoryImpl() }

        // --- 注册 Service (业务逻辑层) ---
        provide<AppConfigService> {
            AppConfigServiceImpl(resolve<AppConfigRepository>())
        }
        provide<AttachFileInfoService> {
            AttachFileInfoServiceImpl(
                resolve<AttachFileInfoRepository>(),
                resolve<S3Tool>()
            )
        }
        provide<AnimeService> {
            AnimeServiceImpl(
                resolve<AnimeRepository>(),
                resolve<AdditionalInfoRepository>(),
                resolve<AttachFileInfoService>()
            )
        }

        // --- 注册 Tools (工具类) ---
        provide<BangumiTool> { BangumiTool() }
        provide<QbTool> { QbTool() }
        provide<TmdbTool> { TmdbTool() }
        provide<S3Tool> { S3Tool() }
    }
}
