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

/**
 * @author SunriseYDY
 * @date 2024-07-24 09:34
 */
class CommonApi internal constructor(private val httpClient: HttpClient) {

    suspend fun getAppInfo(language: Language? = null): Result<AppInfo> =
        httpClient.get(CommonModuleResource.Info(language = language)).body()
    

    suspend fun getLocalizations(): Result<MutableMap<String, String>> =
        httpClient.get(CommonModuleResource.Localization()).body()
    

    suspend fun getAllAppConfigFromDB(): Result<List<AppConfig>> =
        httpClient.get(CommonModuleResource.Config()).body()
    

    suspend fun getAppConfigs(): Result<MutableMap<String, Pair<AppConfig?, String?>>> =
        httpClient.get(CommonModuleResource.Config.Map()).body()
    

    suspend fun saveAppConfigs(configs: List<AppConfig>): Result<List<AppConfig>> =
        httpClient.post(CommonModuleResource.Config()) {
            setBody(configs)
        }.body()
    

    suspend fun getAdditions(
        associatedType: AssociatedTypeInterface,
        associatedId: ULong,
        additionalType: AdditionTypeInterface?
    ): Result<List<AdditionalInfo>> =
        httpClient.get(
            CommonModuleResource.Addition(
                associatedType = associatedType.key,
                associatedId = associatedId,
                additionalType = additionalType?.key
            )
        ).body()

    suspend fun saveAddition(addition: AdditionalInfo): Result<String> =
        httpClient.post(CommonModuleResource.Addition()) {
            setBody(addition)
        }.body()
    

    suspend fun deleteAddition(id: String): Result<Unit> =
        httpClient.delete(CommonModuleResource.Addition(id = id)).body()
    
    suspend fun getAttachFileBytes(id: String): ByteArray =
        httpClient.get(CommonModuleResource.AttachFile(id = id)).body()
}