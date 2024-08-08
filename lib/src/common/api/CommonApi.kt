package dev.sunriseydy.acgn.common.api

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.dto.AppInfo
import dev.sunriseydy.acgn.commonModuleApiEndPoint
import dev.sunriseydy.acgn.interfaces.AdditionTypeInterface
import dev.sunriseydy.acgn.interfaces.AssociatedTypeInterface
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * @author SunriseYDY
 * @date 2024-07-24 09:34
 */
class CommonApi internal constructor(private val httpClient: HttpClient) {

    suspend fun getAppInfo(): Result<AppInfo> = httpClient.get {
        commonModuleApiEndPoint("info")
    }.body()

    suspend fun getLocalizations(): Result<MutableMap<String, String>> = httpClient.get {
        commonModuleApiEndPoint("localization")
    }.body()

    suspend fun getAllAppConfigFromDB(): Result<List<AppConfig>> = httpClient.get {
        configApiEndPoint()
    }.body()

    suspend fun getAppConfigs(): Result<MutableMap<String, Pair<AppConfig?, String?>>> = httpClient.get {
        configApiEndPoint("map")
    }.body()

    suspend fun saveAppConfigs(configs: List<AppConfig>): Result<List<AppConfig>> = httpClient.post {
        configApiEndPoint()
        setBody(configs)
    }.body()

    suspend fun getAdditions(
        associatedType: AssociatedTypeInterface,
        associatedId: ULong,
        additionalType: AdditionTypeInterface?
    ): Result<List<AdditionalInfo>> = httpClient.get {
        additionApiEndPoint()
        parameter("associatedType", associatedType.localizationKey)
        parameter("associatedId", associatedId)
        parameter("additionalType", additionalType?.key)
    }.body()

    suspend fun saveAddition(addition: AdditionalInfo): Result<String> = httpClient.post {
        additionApiEndPoint()
        setBody(addition)
    }.body()

    suspend fun deleteAddition(id: String): Result<Unit> = httpClient.delete {
        additionApiEndPoint()
        parameter("id", id)
    }.body()

    private fun HttpRequestBuilder.configApiEndPoint(vararg paths: String) {
        commonModuleApiEndPoint("config", *paths)
    }

    private fun HttpRequestBuilder.additionApiEndPoint(vararg paths: String) {
        commonModuleApiEndPoint("addition", *paths)
    }
}