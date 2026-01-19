package dev.sunriseydy.acgn.client.anime.enums

import dev.sunriseydy.acgn.base.interfaces.AnimeModule
import dev.sunriseydy.acgn.base.interfaces.EnumKey

/**
 * @author SunriseYDY
 * @date 2024-08-11 22:21
 */
enum class RssString : AnimeModule, EnumKey {
    RSS_TITLE,
    RSS_ITEM_TITLE,
    RSS_READ,
    RSS_FIELD_LINK,
    RSS_FIELD_TITLE,
    IS_ONLY_UNREAD,
    RSS_ITEM_FIELD_PUBLISH_AT,
}