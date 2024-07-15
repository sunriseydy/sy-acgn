package dev.sunriseydy.acgn.anime.repository

import dev.sunriseydy.acgn.anime.db.AnimeDAO
import dev.sunriseydy.acgn.anime.db.AnimeEpisodeDAO
import dev.sunriseydy.acgn.anime.db.AnimeEpisodeTable
import dev.sunriseydy.acgn.anime.db.AnimeSeasonDAO
import dev.sunriseydy.acgn.anime.db.AnimeSeasonTable
import dev.sunriseydy.acgn.anime.db.AnimeTable
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.plugins.suspendTransaction
import org.jetbrains.exposed.sql.SortOrder

/**
 * @author SunriseYDY
 * @date 2024-07-05 10:57
 */
class AnimeRepository {
    suspend fun selectAnimeNameAndId(): Map<String, ULong> = suspendTransaction {
        AnimeTable.select(AnimeTable.name, AnimeTable.id).fold(emptyMap()) { map, anime ->
            map.plus(anime[AnimeTable.name] to anime[AnimeTable.id].value)
        }
    }

    suspend fun selectAnimeById(id: ULong): Anime? = suspendTransaction {
        AnimeDAO.findById(id)?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun selectAnimeSeasonById(id: ULong): AnimeSeason? = suspendTransaction {
        AnimeSeasonDAO.findById(id)?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun selectAnimeSeasonByAnimeId(animeId: ULong): List<AnimeSeason> = suspendTransaction {
        AnimeSeasonDAO.find {
            AnimeSeasonTable.animeId eq animeId
        }.orderBy(AnimeSeasonTable.season to SortOrder.ASC).map(AnimeSeasonDAO::toDTO)
    }

    suspend fun selectAnimeEpisodeById(id: ULong): AnimeEpisode = suspendTransaction {
        AnimeEpisodeDAO.findById(id)?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun selectAnimeEpisodeBySeasonId(seasonId: ULong): List<AnimeEpisode> = suspendTransaction {
        AnimeEpisodeDAO.find {
            AnimeEpisodeTable.animeSeasonId eq seasonId
        }.map(AnimeEpisodeDAO::toDTO)
    }

    suspend fun insertAnime(anime: Anime): Anime = suspendTransaction {
        AnimeDAO.new {
            this.name = anime.name
            this.originalName = anime.originalName
            this.description = anime.description
        }.toDTO()
    }

    suspend fun insertAnimeSeason(animeSeason: AnimeSeason): AnimeSeason = suspendTransaction {
        AnimeSeasonDAO.new {
            this.animeId = animeSeason.animeId
            this.name = animeSeason.name
            this.originalName = animeSeason.originalName
            this.description = animeSeason.description
            this.season = animeSeason.season
            this.year = animeSeason.year
            this.month = animeSeason.month
            this.startedAt = animeSeason.startedAt
            this.endedAt = animeSeason.endedAt
        }.toDTO()
    }

    suspend fun insertAnimeEpisode(animeEpisode: AnimeEpisode): AnimeEpisode = suspendTransaction {
        AnimeEpisodeDAO.new {
            this.animeId = animeEpisode.animeId
            this.animeSeasonId = animeEpisode.animeSeasonId
            this.name = animeEpisode.name
            this.originalName = animeEpisode.originalName
            this.description = animeEpisode.description
            this.episode = animeEpisode.episode
            this.publishedAt = animeEpisode.publishedAt
        }.toDTO()
    }

    suspend fun deleteAnimeById(id: ULong): Unit = suspendTransaction {
        AnimeDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }

    suspend fun deleteAnimeSeasonById(id: ULong): Unit = suspendTransaction {
        AnimeSeasonDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }

    suspend fun deleteAnimeSeasonByAnimeId(animeId: ULong): Unit = suspendTransaction {
        AnimeSeasonDAO.find {
            AnimeSeasonTable.animeId eq animeId
        }.forEach {
            it.delete()
        }
    }

    suspend fun deleteAnimeEpisodeById(id: ULong): Unit = suspendTransaction {
        AnimeEpisodeDAO.findById(id)?.delete() ?: throw NoSuchElementException()
    }

    suspend fun deleteAnimeEpisodeBySeasonId(seasonId: ULong): Unit = suspendTransaction {
        AnimeEpisodeDAO.find {
            AnimeEpisodeTable.animeSeasonId eq seasonId
        }.forEach {
            it.delete()
        }
    }

    suspend fun deleteAnimeEpisodeByAnimeId(animeId: ULong): Unit = suspendTransaction {
        AnimeEpisodeDAO.find {
            AnimeEpisodeTable.animeId eq animeId
        }.forEach {
            it.delete()
        }
    }
}