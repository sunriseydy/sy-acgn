package dev.sunriseydy.acgn.anime.tools

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.common.config.AnimeModuleAppConfig
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * @author SunriseYDY
 * @date 2024-07-21 20:37
 */
class FileTool {

    fun handleAnimeSeasonFile(animeSeason: AnimeSeason, path: String) {
        requireNotNull(animeSeason.anime) { "动画数据不能为空" }
        require(exists(path) && isDirectory(path)) { "路径 $path 不存在或不是文件夹" }
        val fileNames = listFileNames(path)
        check(fileNames.isNotEmpty()) { "路径 $path 下没有文件" }
        val videoNames = listVideoNames(fileNames)
        check(videoNames.isNotEmpty()) { "路径 $path 下没有视频文件" }
        val subtitleNames = listSubtitleNames(fileNames)
        val otherNames = fileNames - videoNames - subtitleNames
        val mediaTargetDirectory = AnimeModuleAppConfig.MediaTargetDirectory.configValue
        requireNotNull(mediaTargetDirectory) { "${AnimeModuleAppConfig.MediaTargetDirectory.configKey}-媒体库目录不能为空" }
        val animeDirectoryName = generateAnimeDirectoryName(animeSeason.anime!!)
        val seasonDirectoryName = generateSeasonDirectoryName(animeSeason)

    }

    fun isVideo(path: String) = Extension.VIDEO.isPathMatch(path)
    fun isSubtitle(path: String) = Extension.SUBTITLE.isPathMatch(path)
    fun isDirectory(path: String) = SystemFileSystem.metadataOrNull(Path(path))?.isDirectory == true
    fun exists(path: String) = SystemFileSystem.exists(Path(path))
    fun moveFile(from: String, to: String) {
        SystemFileSystem.atomicMove(Path(from), Path(to))
    }

    fun createDirectories(path: String) {
        SystemFileSystem.createDirectories(Path(path))
    }

    fun listFileNames(path: String) = SystemFileSystem.list(Path(path)).map { it.name }.sorted()
    fun listVideoNames(path: String) = listFileNames(path).filter { isVideo(it) }
    fun listVideoNames(fileNames: List<String>) = fileNames.filter { isVideo(it) }
    fun listSubtitleNames(path: String) = listFileNames(path).filter { isSubtitle(it) }
    fun listSubtitleNames(fileNames: List<String>) = fileNames.filter { isSubtitle(it) }
    fun generateAnimeDirectoryName(anime: Anime) =
        buildString {
            append(anime.name)
            anime.startedAt?.let { append("(${anime.startedAt!!.toLocalDateTime(TimeZone.currentSystemDefault()).year})") }
            append(" ")
            anime.tmdbId?.let { append("[tmdbid-${anime.tmdbId}]") }
        }

    fun generateSeasonDirectoryName(animeSeason: AnimeSeason) =
        "Season ${animeSeason.season.toString().padStart(2, '0')}"

    fun generateEpisodeVideoFileNameListByNameSort(videos: List<String>, season: Int) =
        videos.mapIndexed { index, video ->
            video to "S${season.toString().padStart(2, '0')}E${(index + 1).toString().padStart(2, '0')} $video"
        }

    fun generateEpisodeSubtitleFileNameByVideoFileName(videos: List<Pair<String, String>>, subtitles: List<String>) =
        check(videos.size == subtitles.size) { "videos and subtitles size not match" }
            .run {
                videos.mapIndexed { index, video ->
                    val subtitle = subtitles[index]
                    val subtitleExt = subtitle.substringAfterLast(".")
                    val videoName = video.second.substringBeforeLast(".")
                    return@mapIndexed subtitle to "$videoName.$subtitleExt"
                }
            }

    fun generateOtherFileName(others: List<String>): Nothing =
        TODO()

}

enum class Extension(val exts: Set<String>) {
    VIDEO(setOf("mp4", "mkv")),
    SUBTITLE(setOf("ass", "srt"))
    ;

    fun isExtMatch(ext: String) = exts.contains(ext)
    fun isPathMatch(path: String) = isExtMatch(path.substringAfterLast("."))
}