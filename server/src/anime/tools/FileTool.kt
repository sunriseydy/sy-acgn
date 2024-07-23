package dev.sunriseydy.acgn.anime.tools

import dev.sunriseydy.acgn.anime.dto.Anime
import dev.sunriseydy.acgn.anime.dto.AnimeSeason
import dev.sunriseydy.acgn.anime.dto.AnimeSeasonFile
import dev.sunriseydy.acgn.common.config.AnimeModuleAppConfig
import dev.sunriseydy.acgn.exception.AnimeModuleException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * @author SunriseYDY
 * @date 2024-07-21 20:37
 */
class FileTool {

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
        val subtitles = listSubtitles(fileNames)
        val others = fileNames - videos - subtitles
        val mediaTargetDirectory = AnimeModuleAppConfig.MediaTargetDirectory.configValue
        requireNotNull(mediaTargetDirectory) { "${AnimeModuleAppConfig.MediaTargetDirectory.configKey}-媒体库目录不能为空" }
        // 生成目录名
        val animeDirectoryName = generateAnimeDirectoryName(animeSeason.anime!!)
        val seasonDirectoryName = generateSeasonDirectoryName(animeSeason)
        val animeSeasonDirectory = Path(mediaTargetDirectory, animeDirectoryName, seasonDirectoryName)
        // 生成文件名
        val newVideoPairs = generateEpisodeVideosByNameSort(videos, animeSeason.season, animeSeasonDirectory)
        val newSubtitlePairs =
            generateEpisodeSubtitlesByVideos(newVideoPairs, subtitles, animeSeasonDirectory)
        val newOtherPairs = generateOthers(others, animeSeasonDirectory)
        if (exists(animeSeasonDirectory) && isDirectory(animeSeasonDirectory)) {
            if (animeSeasonFile.isDeleteTarget) {
                deleteFile(animeSeasonDirectory)
            } else {
                throw AnimeModuleException("target_dir_exists")
            }
        }
        // 创建目录
        if (others.any { !isDirectory(it) }) {
            createDirectories(Path(animeSeasonDirectory, SP_DIR_NAME))
        } else {
            createDirectories(animeSeasonDirectory)
        }
        // 移动文件
        listOf(
            *newVideoPairs.toTypedArray(),
            *newSubtitlePairs.toTypedArray(),
            *newOtherPairs.toTypedArray()
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
    fun moveFile(from: Path, to: Path) = SystemFileSystem.atomicMove(from, to)
    fun createDirectories(path: Path) = SystemFileSystem.createDirectories(path)
    fun deleteFile(path: Path) = SystemFileSystem.delete(path)
    fun listFiles(path: Path) = SystemFileSystem.list(path).sortedBy { it.name }
    fun listVideos(files: List<Path>) = files.filter { isVideo(it) }
    fun listSubtitles(files: List<Path>) = files.filter { isSubtitle(it) }
    fun generateAnimeDirectoryName(anime: Anime) =
        buildString {
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
        videos: List<Pair<Path, Path>>,
        subtitles: List<Path>,
        animeSeasonDirectory: Path
    ) =
        check(videos.size == subtitles.size) { "videos and subtitles size not match" }
            .run {
                videos.mapIndexed { index, video ->
                    val subtitle = subtitles[index]
                    val subtitleExt = subtitle.name.substringAfterLast(".")
                    val videoName = video.second.name.substringBeforeLast(".")
                    return@mapIndexed subtitle to Path(animeSeasonDirectory, "$videoName.$subtitleExt")
                }
            }

    fun generateOthers(others: List<Path>, animeSeasonDirectory: Path) =
        others.map {
            if (isDirectory(it)) {
                it to Path(animeSeasonDirectory, ".${it.name}")
            } else {
                it to Path(animeSeasonDirectory, SP_DIR_NAME, it.name)
            }
        }

}

enum class Extension(val exts: Set<String>) {
    VIDEO(setOf("mp4", "mkv")),
    SUBTITLE(setOf("ass", "srt"))
    ;

    fun isExtMatch(ext: String) = exts.contains(ext)
    fun isPathMatch(path: String) = isExtMatch(path.substringAfterLast("."))
}