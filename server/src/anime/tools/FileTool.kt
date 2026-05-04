package dev.sunriseydy.acgn.server.anime.tools

import io.github.oshai.kotlinlogging.KotlinLogging
import dev.sunriseydy.acgn.anime.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.anime.enums.AnimeModuleError
import dev.sunriseydy.acgn.base.exception.MessageException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

private val logger = KotlinLogging.logger { }

/**
 * @author SunriseYDY
 * @date 2024-07-21 20:37
 */
object FileTool {

    val SP_DIR_NAME = ".SPs"

    fun handleAnimeSeasonFile(
        animeSeason: AnimeSeason,
        animeSeasonFile: AnimeSeasonFile,
    ) {
        requireNotNull(animeSeason.anime) { "动画数据不能为空" }
        val path = Path(animeSeasonFile.path)
        require(exists(path) && isDirectory(path)) { "路径 $path 不存在或不是文件夹" }
        val fileNames = listFiles(path)
        check(fileNames.isNotEmpty()) { "路径 $path 下没有文件" }
        val videos = listVideos(fileNames)
        check(videos.isNotEmpty()) { "路径 $path 下没有视频文件" }
        val allSubtitles = listSubtitles(fileNames)
        // 忽略非简体中文字幕文件
        val deleteSubtitles = listDeleteSubtitles(fileNames)
        val subtitles = allSubtitles - deleteSubtitles.toSet()
        val others = fileNames - videos.toSet() - allSubtitles.toSet()
        // 获取媒体目录
        val mediaTargetDirectory = AnimeModuleAppConfig.MediaTargetDirectory.configValue ?: path.parent.toString()
        requireNotNull(mediaTargetDirectory) { "${AnimeModuleAppConfig.MediaTargetDirectory.configKey}-媒体库目录不能为空" }
        // 生成目录名
        val animeDirectoryName = generateAnimeDirectoryName(animeSeason.anime!!)
        val seasonDirectoryName = generateSeasonDirectoryName(animeSeason)
        val animeSeasonDirectory = Path(mediaTargetDirectory, animeDirectoryName, seasonDirectoryName)
        // 生成文件名
        val newVideoPairs = generateEpisodeVideosByNameSort(videos, animeSeason.season, animeSeasonDirectory)
        val newSubtitlePairs = if (subtitles.isNotEmpty()) {
            generateEpisodeSubtitlesByVideos(newVideoPairs, subtitles, animeSeasonDirectory)
        } else emptyList()
        val newOtherPairs = generateOthers(others, animeSeasonDirectory)
        if (exists(animeSeasonDirectory) && isDirectory(animeSeasonDirectory)) {
            if (animeSeasonFile.isDeleteTarget) {
                deleteFile(animeSeasonDirectory)
            } else {
                throw MessageException(AnimeModuleError.TARGET_DIR_EXISTS)
            }
        }
        // 创建目录
        createDirectories(animeSeasonDirectory)
        if (others.any { !isDirectory(it) }) {
            // 创建 .SP 目录
            createDirectories(Path(animeSeasonDirectory, SP_DIR_NAME))
        }
        // 移动文件
        listOf(
            *newVideoPairs.toTypedArray(), *newSubtitlePairs.toTypedArray(), *newOtherPairs.toTypedArray()
        ).forEach {
            moveFile(it.first, it.second)
        }
        // 删除文件
        if (animeSeasonFile.isDeleteSource) deleteFile(path)
    }

    fun isVideo(path: Path) = Extension.VIDEO.isPathMatch(path.name)
    fun isSubtitle(path: Path) = Extension.SUBTITLE.isPathMatch(path.name)
    fun isDirectory(path: Path) = SystemFileSystem.metadataOrNull(path)?.isDirectory == true
    fun exists(path: Path) = SystemFileSystem.exists(path)
    fun moveFile(from: Path, to: Path) {
        logger.info { "Moving file from: $from to: $to" }
        SystemFileSystem.atomicMove(from, to)
    }
    fun createDirectories(path: Path) {
        logger.info { "Creating directories: $path" }
        SystemFileSystem.createDirectories(path)
    }
    fun deleteFile(path: Path) {
        logger.info { "Deleting file: $path" }
        SystemFileSystem.delete(path)
    }
    fun listFiles(path: Path) = SystemFileSystem.list(path).sortedBy { it.name }
    fun listVideos(files: List<Path>) = files.filter { isVideo(it) }
    fun listSubtitles(files: List<Path>) = files.filter { isSubtitle(it) }
    fun listDeleteSubtitles(files: List<Path>) = files.filter { isSubtitle(it) && it.name.contains(".tc.") }
    fun generateAnimeDirectoryName(anime: Anime) = buildString {
        append(anime.name)
        anime.firstAirDate?.let { append(" (${anime.firstAirDate!!.year})") }
        anime.tmdbId?.let { append(" [tmdbid-${anime.tmdbId}]") }
    }

    fun generateSeasonDirectoryName(animeSeason: AnimeSeason) =
        "Season ${animeSeason.season.toString().padStart(2, '0')}"

    fun generateEpisodeVideosByNameSort(videos: List<Path>, season: Int, animeSeasonDirectory: Path) =
        videos.mapIndexed { index, video ->
            val episodeFileName = buildString {
                append("S${season.toString().padStart(2, '0')}")
                append("E${(index + 1).toString().padStart(2, '0')}")
                append(" ")
                append(video.name)
            }
            video to Path(animeSeasonDirectory, episodeFileName)
        }

    fun generateEpisodeSubtitlesByVideos(
        videos: List<Pair<Path, Path>>, subtitles: List<Path>, animeSeasonDirectory: Path
    ) = check(videos.size == subtitles.size) { "videos and subtitles size not match" }.run {
        videos.mapIndexed { index, video ->
            val subtitle = subtitles[index]
            val subtitleExt = subtitle.name.substringAfterLast(".")
            val videoName = video.second.name.substringBeforeLast(".")
            return@mapIndexed subtitle to Path(animeSeasonDirectory, "$videoName.$subtitleExt")
        }
    }

    fun generateOthers(others: List<Path>, animeSeasonDirectory: Path) = others.map {
        if (isDirectory(it)) {
            it to Path(animeSeasonDirectory, ".${it.name}")
        } else {
            it to Path(animeSeasonDirectory, SP_DIR_NAME, it.name)
        }
    }

}

enum class Extension(val exts: Set<String>) {
    VIDEO(setOf("mp4", "mkv")), SUBTITLE(setOf("ass", "srt"));

    fun isExtMatch(ext: String) = exts.contains(ext)
    fun isPathMatch(path: String) = isExtMatch(path.substringAfterLast("."))
}