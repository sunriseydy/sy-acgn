package dev.sunriseydy.acgn.anime.repository

import dev.sunriseydy.acgn.anime.db.AnimeDAO
import dev.sunriseydy.acgn.anime.db.AnimeEpisodeDAO
import dev.sunriseydy.acgn.anime.db.AnimeEpisodeTable
import dev.sunriseydy.acgn.anime.db.AnimeSeasonDAO
import dev.sunriseydy.acgn.anime.db.AnimeSeasonTable
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.plugins.suspendTransaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and

/**
 * @author SunriseYDY
 * @date 2024-07-05 10:57
 */
class AnimeRepository {
    suspend fun selectAllAnime() = suspendTransaction {
        AnimeDAO.all().map(AnimeDAO::toDTO)
    }

    suspend fun selectAnimeById(id: ULong): Anime? = suspendTransaction {
        AnimeDAO.findById(id)?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun selectAnimeSeasonById(id: ULong): AnimeSeason = suspendTransaction {
        AnimeSeasonDAO.findById(id)?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun selectAnimeSeasonByAnimeId(animeId: ULong): List<AnimeSeason> = suspendTransaction {
        AnimeSeasonDAO.find {
            AnimeSeasonTable.animeId eq animeId
        }.orderBy(AnimeSeasonTable.season to SortOrder.ASC).map(AnimeSeasonDAO::toDTO)
    }

    suspend fun selectAnimeSeasonsByYearAndMonth(year: Int, month: List<Int>) = suspendTransaction {
        AnimeSeasonDAO.find {
            (AnimeSeasonTable.year eq year) and (AnimeSeasonTable.month inList month)
        }.orderBy(AnimeSeasonTable.airDate to SortOrder.ASC).map(AnimeSeasonDAO::toDTO)
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
            this.airDate = anime.airDate
            this.tmdbId = anime.tmdbId
            this.bgmId = anime.bgmId
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
            this.airDate = animeSeason.airDate
            this.tmdbId = animeSeason.tmdbId
            this.bgmId = animeSeason.bgmId
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
            this.airDate = animeEpisode.airDate
            this.tmdbId = animeEpisode.tmdbId
            this.bgmId = animeEpisode.bgmId
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