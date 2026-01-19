package dev.sunriseydy.acgn.client.common.api

import dev.sunriseydy.acgn.base.Result
import dev.sunriseydy.acgn.base.enums.Language
import dev.sunriseydy.acgn.base.interfaces.AdditionTypeInterface
import dev.sunriseydy.acgn.base.interfaces.AssociatedTypeInterface
import dev.sunriseydy.acgn.common.CommonModuleResource
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.dto.AppInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-07-24 09:34
 */
class CommonApi internal constructor(private val httpClient: HttpClient) {

    fun getAppInfo(language: Language? = null): Result<AppInfo> = runBlocking {
        httpClient.get(CommonModuleResource.Info(language = language)).body()
    }

    fun getLocalizations(): Result<MutableMap<String, String>> = runBlocking {
        httpClient.get(CommonModuleResource.Localization()).body()
    }

    fun getAllAppConfigFromDB(): Result<List<AppConfig>> = runBlocking {
        httpClient.get(CommonModuleResource.Config()).body()
    }

    fun getAppConfigs(): Result<MutableMap<String, Pair<AppConfig?, String?>>> = runBlocking {
        httpClient.get(CommonModuleResource.Config.Map()).body()
    }

    fun saveAppConfigs(configs: List<AppConfig>): Result<List<AppConfig>> = runBlocking {
        httpClient.post(CommonModuleResource.Config()) {
            setBody(configs)
        }.body()
    }

    fun getAdditions(
        associatedType: AssociatedTypeInterface,
        associatedId: ULong,
        additionalType: AdditionTypeInterface?
    ): Result<List<AdditionalInfo>> = runBlocking {
        httpClient.get(
            CommonModuleResource.Addition(
                associatedType = associatedType.key,
                associatedId = associatedId,
                additionalType = additionalType?.key
            )
        ).body()
    }

    fun saveAddition(addition: AdditionalInfo): Result<String> = runBlocking {
        httpClient.post(CommonModuleResource.Addition()) {
            setBody(addition)
        }.body()
    }

    fun deleteAddition(id: String): Result<Unit> = runBlocking {
        httpClient.delete(CommonModuleResource.Addition(id = id)).body()
    }
}