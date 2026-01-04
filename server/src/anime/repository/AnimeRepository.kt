package dev.sunriseydy.acgn.server.anime.repository

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeEpisode
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.server.anime.db.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

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

    suspend fun selectAnimeSeasonYears() = suspendTransaction {
        AnimeSeasonTable.select(AnimeSeasonTable.year)
            .withDistinct(true)
            .map { it[AnimeSeasonTable.year] }
            .sortedDescending()
            .toList()
    }

    suspend fun selectAnimeSeasonsByYearAndMonth(year: Int, months: List<Int>?) = suspendTransaction {
        AnimeSeasonDAO.find {
            (AnimeSeasonTable.year eq year) and (months?.let { AnimeSeasonTable.month inList it } ?: Op.TRUE)
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
            this.description = anime.description
            this.firstAirDate = anime.firstAirDate
            this.lastAirDate = anime.lastAirDate
            this.numberOfSeasons = anime.numberOfSeasons
            this.numberOfEpisodes = anime.numberOfEpisodes
            this.tmdbId = anime.tmdbId
            this.bgmId = anime.bgmId
        }.toDTO()
    }

    suspend fun insertAnimeSeason(animeSeason: AnimeSeason): AnimeSeason = suspendTransaction {
        AnimeSeasonDAO.new {
            this.animeId = animeSeason.animeId
            this.name = animeSeason.name
            this.description = animeSeason.description
            this.season = animeSeason.season
            this.numberOfEpisodes = animeSeason.numberOfEpisodes
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
            this.description = animeEpisode.description
            this.episode = animeEpisode.episode
            this.airDate = animeEpisode.airDate
            this.tmdbId = animeEpisode.tmdbId
            this.bgmId = animeEpisode.bgmId
        }.toDTO()
    }

    suspend fun updateAnime(anime: Anime): Anime = suspendTransaction {
        AnimeDAO.findByIdAndUpdate(anime.id) {
            it.name = anime.name
            it.description = anime.description
            it.firstAirDate = anime.firstAirDate
            it.lastAirDate = anime.lastAirDate
            it.numberOfSeasons = anime.numberOfSeasons
            it.numberOfEpisodes = anime.numberOfEpisodes
            anime.tmdbId?.apply { it.tmdbId = anime.tmdbId }
            anime.bgmId?.apply { it.bgmId = anime.bgmId }
        }?.toDTO() ?: throw NoSuchElementException()
    }

    suspend fun updateAnimeSeason(animeSeason: AnimeSeason): AnimeSeason = suspendTransaction {
        AnimeSeasonDAO.findByIdAndUpdate(animeSeason.id) {
            it.animeId = animeSeason.animeId
            it.name = animeSeason.name
            it.description = animeSeason.description
            it.season = animeSeason.season
            it.numberOfEpisodes = animeSeason.numberOfEpisodes
            it.year = animeSeason.year
            it.month = animeSeason.month
            it.airDate = animeSeason.airDate
            animeSeason.tmdbId?.apply { it.tmdbId = animeSeason.tmdbId }
            animeSeason.bgmId?.apply { it.bgmId = animeSeason.bgmId }
        }?.toDTO() ?: throw NoSuchElementException()
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