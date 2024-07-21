package dev.sunriseydy.acgn.anime.tools

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * @author SunriseYDY
 * @date 2024-07-21 20:37
 */
class FileTool {

    fun isVideo(path: String) = Extension.VIDEO.isPathMatch(path)
    fun isSubtitle(path: String) = Extension.SUBTITLE.isPathMatch(path)
    fun isDirectory(path: String) = SystemFileSystem.metadataOrNull(Path(path))?.isDirectory == true
    fun exists(path: String) = SystemFileSystem.exists(Path(path))
    fun listFiles(path: String) = SystemFileSystem.list(Path(path)).sortedBy { it.name }
    fun listVideos(path: String) = listFiles(path).filter { isVideo(it.name) }
    fun listSubtitles(path: String) = listFiles(path).filter { isSubtitle(it.name) }
    fun generateAnimeDirectoryName(anime: Anime) =
        buildString {
            append(anime.name)
            anime.startedAt?.let { append("(${anime.startedAt!!.toLocalDateTime(TimeZone.currentSystemDefault()).year})") }
            append(" ")
            anime.tmdbId?.let { append("[tmdbid-${anime.tmdbId}]") }
        }

    fun generateSeasonDirectoryName(animeSeason: AnimeSeason) =
        "Season ${animeSeason.season.toString().padStart(2, '0')}"

    fun generateEpisodeFileNameListByNameSort(videos: List<Path>, season: Int) =
        videos.mapIndexed { index, videoFilePath ->
            "S${season.toString().padStart(2, '0')}E${(index + 1).toString().padStart(2, '0')} ${videoFilePath.name}"
        }
}

enum class Extension(val exts: Set<String>) {
    VIDEO(setOf("mp4", "mkv")),
    SUBTITLE(setOf("ass", "srt"))
    ;

    fun isExtMatch(ext: String) = exts.contains(ext)
    fun isPathMatch(path: String) = isExtMatch(path.substringAfterLast("."))
}