package dev.sunriseydy.acgn.client.common.api

import dev.sunriseydy.acgn.Result
import dev.sunriseydy.acgn.client.commonModuleApiEndPoint
import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.common.dto.AppConfig
import dev.sunriseydy.acgn.common.dto.AppInfo
import dev.sunriseydy.acgn.enums.Language
import dev.sunriseydy.acgn.interfaces.AdditionTypeInterface
import dev.sunriseydy.acgn.interfaces.AssociatedTypeInterface
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

/**
 * @author SunriseYDY
 * @date 2024-07-24 09:34
 */
class CommonApi internal constructor(private val httpClient: HttpClient) {

    fun getAppInfo(language: Language? = null): Result<AppInfo> = runBlocking {
        httpClient.get {
            commonModuleApiEndPoint("info")
            language?.let { parameter("language", language.name) }
        }.body()
    }

    fun getLocalizations(): Result<MutableMap<String, String>> = runBlocking {
        httpClient.get {
            commonModuleApiEndPoint("localization")
        }.body()
    }

    fun getAllAppConfigFromDB(): Result<List<AppConfig>> = runBlocking {
        httpClient.get {
            configApiEndPoint()
        }.body()
    }

    fun getAppConfigs(): Result<MutableMap<String, Pair<AppConfig?, String?>>> = runBlocking {
        httpClient.get {
            configApiEndPoint("map")
        }.body()
    }

    fun saveAppConfigs(configs: List<AppConfig>): Result<List<AppConfig>> = runBlocking {
        httpClient.post {
            configApiEndPoint()
            setBody(configs)
        }.body()
    }

    fun getAdditions(
        associatedType: AssociatedTypeInterface,
        associatedId: ULong,
        additionalType: AdditionTypeInterface?
    ): Result<List<AdditionalInfo>> = runBlocking {
        httpClient.get {
            additionApiEndPoint()
            parameter("associatedType", associatedType.key)
            parameter("associatedId", associatedId)
            parameter("additionalType", additionalType?.key)
        }.body()
    }

    fun saveAddition(addition: AdditionalInfo): Result<String> = runBlocking {
        httpClient.post {
            additionApiEndPoint()
            setBody(addition)
        }.body()
    }

    fun deleteAddition(id: String): Result<Unit> = runBlocking {
        httpClient.delete {
            additionApiEndPoint()
            parameter("id", id)
        }.body()
    }

    private fun HttpRequestBuilder.configApiEndPoint(vararg paths: String) {
        commonModuleApiEndPoint("config", *paths)
    }

    private fun HttpRequestBuilder.additionApiEndPoint(vararg paths: String) {
        commonModuleApiEndPoint("addition", *paths)
    }
}