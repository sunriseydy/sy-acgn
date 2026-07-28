package dev.sunriseydy.acgn.server.game.tools

import dev.sunriseydy.acgn.common.dto.AdditionalInfo
import dev.sunriseydy.acgn.game.dto.Game
import dev.sunriseydy.acgn.game.dto.GameRelease
import dev.sunriseydy.acgn.game.enums.GameAdditionType
import dev.sunriseydy.acgn.game.enums.GameAssociatedType
import dev.sunriseydy.acgn.game.enums.GamePlatformEnum
import dev.sunriseydy.acgn.tools.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
data class SteamSearchItem(
    val id: ULong,
    val name: String,
    val tiny_image: String? = null,
)

@Serializable
data class SteamSearchResponse(
    val total: Int = 0,
    val items: List<SteamSearchItem> = emptyList(),
)

class SteamTool {
    private val client = HttpClientFactory.buildHttpClient {
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun searchGame(query: String): List<Game> {
        return try {
            val response: SteamSearchResponse = client.get("https://store.steampowered.com/api/storesearch/") {
                parameter("term", query)
                parameter("l", "schinese")
                parameter("cc", "CN")
            }.body()

            response.items.map { item ->
                Game(
                    id = ULong.MIN_VALUE,
                    name = item.name,
                    steamId = item.id,
                    releases = listOf(
                        GameRelease(
                            id = ULong.MIN_VALUE,
                            gameId = ULong.MIN_VALUE,
                            platform = GamePlatformEnum.STEAM.name
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAppDetail(appId: ULong): Game {
        val rootObj: JsonObject = client.get("https://store.steampowered.com/api/appdetails") {
            parameter("appids", appId.toString())
            parameter("l", "schinese")
        }.body()

        val appObj = rootObj[appId.toString()]?.jsonObject
        val dataObj = appObj?.get("data")?.jsonObject ?: throw NoSuchElementException("Steam App details not found for appId $appId")

        val name = dataObj["name"]?.toString()?.removeSurrounding("\"") ?: ""
        val description = dataObj["detailed_description"]?.toString()?.removeSurrounding("\"")
        val developers = dataObj["developers"]?.toString()?.removeSurrounding("[")?.removeSurrounding("]")?.removeSurrounding("\"")
        val publishers = dataObj["publishers"]?.toString()?.removeSurrounding("[")?.removeSurrounding("]")?.removeSurrounding("\"")

        val additions = listOf(
            AdditionalInfo(
                "", ULong.MIN_VALUE, GameAssociatedType.GAME.key,
                GameAdditionType.SteamJson.key,
                dataObj.toString()
            )
        )

        return Game(
            id = ULong.MIN_VALUE,
            name = name,
            developer = developers,
            publisher = publishers,
            description = description,
            steamId = appId,
            releases = listOf(
                GameRelease(
                    id = ULong.MIN_VALUE,
                    gameId = ULong.MIN_VALUE,
                    platform = GamePlatformEnum.STEAM.name
                )
            ),
            additions = additions
        )
    }
}
