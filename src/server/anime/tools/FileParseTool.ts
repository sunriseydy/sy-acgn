import fs from 'fs/promises' // 使用fs的Promise版本
import { existsSync, statSync } from 'fs'
import parseFileName from './anime-file-parser/fileParser'
import { AnimeInfo } from '@server/anime/tools/anime-file-parser/AnimeInfo'
import config from '@server/config'
import Path from 'node:path'

export default class FileParseTool {
  static VIDEO_EXTENSIONS = new Set(['mp4', 'mkv'])
  static SUBTITLE_EXTENSIONS = new Set(['srt', 'ass'])
  static TORRENT_EXTENSIONS = new Set(['torrent'])

  readonly parseFileName = parseFileName

  /**
   * 获取目录下的所有文件
   * @param dir
   */
  async getFileInDirectoryAsync(dir: string): Promise<string[]> {
    try {
      // 直接返回Promise，避免手动创建
      return fs.readdir(dir)
    } catch (err) {
      // 更现代的错误处理方式
      throw new Error(`Unable to read directory: ${dir}`)
    }
  }

  /**
   * 判断文件后缀是否匹配
   * @param fileName
   * @param extensions
   * @private
   */
  private static isExtensionMatch(fileName: string, extensions: Set<string>): boolean {
    const extension = Path.extname(fileName).toLowerCase().slice(1)
    return extensions.has(extension)
  }

  /**
   * 判断文件是否是视频文件
   * @param fileName
   */
  isVideoFile(fileName: string): boolean {
    return FileParseTool.isExtensionMatch(fileName, FileParseTool.VIDEO_EXTENSIONS)
  }

  /**
   * 判断文件是否是字幕文件
   * @param fileName
   */
  isSubtitleFile(fileName: string): boolean {
    return FileParseTool.isExtensionMatch(fileName, FileParseTool.SUBTITLE_EXTENSIONS)
  }

  isTorrentFile(fileName: string): boolean {
    return FileParseTool.isExtensionMatch(fileName, FileParseTool.TORRENT_EXTENSIONS)
  }

  async getAllVideoFile(dir: string) {
    const files = await this.getFileInDirectoryAsync(dir)
    return files.filter((file) => this.isVideoFile(file))
  }

  async getAllSubtitleFile(dir: string) {
    const files = await this.getFileInDirectoryAsync(dir)
    return files.filter((file) => this.isSubtitleFile(file))
  }

  async getAllDirectory(dir: string) {
    const files = await this.getFileInDirectoryAsync(dir)
    return files.filter((file) => this.isDirectory(file))
  }

  /**
   * 是否是全季
   * @param dir
   */
  async isAllSeasons(dir: string) {
    if (!this.isDirectory(dir)) {
      // 如果不是目录，则不是
      return false
    }
    if (await this.hasVideoFile(dir)) {
      // 如果目录中含有视频文件，则不是
      return false
    }
    const files = await this.getFileInDirectoryAsync(dir)
    // 如果目录中全部是子目录，则是
    return files.every((file) => this.isDirectory(file))
  }

  async hasVideoFile(dir: string) {
    const files = await this.getFileInDirectoryAsync(dir)
    return files.some((file) => this.isVideoFile(file))
  }

  isFileExists(fileName: string): boolean {
    try {
      return existsSync(fileName)
    } catch (err) {
      // 静默处理可能的错误，返回false视为文件不存在
      return false
    }
  }

  isFile(fileName: string): boolean {
    return this.isFileExists(fileName) && statSync(fileName).isFile()
  }

  isDirectory(fileName: string): boolean {
    return this.isFileExists(fileName) && statSync(fileName).isDirectory()
  }

  getDirNameFromPath(path: string) {
    if (!path) {
      return ''
    }
    if (this.isDirectory(path)) {
      if (path.endsWith(Path.sep)) {
        return path.substring(0, path.length - 1)
      } else {
        return path
      }
    } else {
      return Path.dirname(path)
    }
  }

  getFileNameFromPath(path: string) {
    return Path.basename(path)
  }

  getFileExtension(fileName: string) {
    return Path.extname(fileName)
  }

  getFileNameWithoutExtension(fileName: string) {
    return Path.basename(fileName, Path.extname(fileName))
  }

  async moveFile(source: string, target: string, overwrite: boolean = true): Promise<void> {
    if (!overwrite && this.isFile(target)) {
      console.warn(target, ' File already exists')
      return
    }
    try {
      await fs.rename(source, target)
    } catch (err: any) {
      throw new Error(`Failed to move file from ${source} to ${target}: ${err.message || err}`)
    }
  }

  async moveFiles(sources: string[], targets: string[], dir: string, overwrite: boolean = true) {
    if (sources.length !== targets.length) {
      throw 'sources and targets must have the same length'
    }
    for (let i = 0; i < sources.length; i++) {
      await this.moveFile(
        Path.isAbsolute(sources[i]) ? sources[i] : Path.join(dir, sources[i]),
        Path.isAbsolute(targets[i]) ? targets[i] : Path.join(dir, targets[i]),
        overwrite,
      )
    }
  }

  generateAnimeDirectoryName(animeInfo: AnimeInfo) {
    const { tvDetail } = animeInfo
    if (!tvDetail || !tvDetail.name || !tvDetail.id) {
      throw 'TMDB 动画数据字段缺少'
    }
    const year = tvDetail.first_air_date ? tvDetail.first_air_date.substring(0, 3) || '' : ''
    const season = `${animeInfo.season}`.padStart(2, '0')
    // 格式为 `animeTitle(year)[tmdbid-xxxx]/Season xx`
    return Path.join(
      `${animeInfo.animeTitle}${year ? '(' + year + ')' : ''}[tmdbid-${tvDetail.id}]`,
      `Season ${season}`,
    )
  }

  generateAnimeFileName(file: string, animeInfo: AnimeInfo) {
    const fileName = this.getFileNameFromPath(file)
    // 格式为 S%02dE%02d fileName
    const season = `${animeInfo.season}`.padStart(2, '0')
    const episode = `${animeInfo.episode}`.padStart(2, '0')
    return `S${season}E${episode} ${fileName}`
  }

  generateVideoFileNameByFileNameSort(files: string[], animeInfo: AnimeInfo) {
    const season = `${animeInfo.season}`.padStart(2, '0')
    const result = []
    for (let i = 0; i < files.length; i++) {
      const episode = `${i + 1}`.padStart(2, '0')
      const prefix = `S${season}E${episode}`
      const fileName = this.getFileNameFromPath(files[i])
      if (fileName.includes(prefix)) {
        result.push(fileName)
      } else {
        result.push(prefix + ' ' + fileName)
      }
    }
    return result
  }

  generateSubtitleFileNameByFileNameSort(subtitles: string[], videos: string[]) {
    if (videos.length !== subtitles.length) {
      throw '视频和字幕数量不匹配'
    }
    const result = []
    for (let i = 0; i < subtitles.length; i++) {
      const subtitleName = subtitles[i]
      const videoName = videos[i]
      const videoNameWithoutExtension = this.getFileNameWithoutExtension(videoName)
      const subtitleExtension = this.getFileExtension(subtitleName)
      result.push(videoNameWithoutExtension + '.' + subtitleExtension)
    }
    return result
  }

  generateTargetDirectory(animeInfo: AnimeInfo) {
    const animeDirectoryName = this.generateAnimeDirectoryName(animeInfo)
    const targetPath = config.anime.path.targetPath
    if (!targetPath) {
      throw '未配置目标路径'
    }
    return Path.join(targetPath, animeDirectoryName)
  }

  /**
   * 创建目录,递归
   * @param dir
   */
  async createDirectory(dir: string): Promise<void> {
    if (this.isDirectory(dir)) {
      return
    }
    try {
      await fs.mkdir(dir, { recursive: true })
    } catch (err: any) {
      throw new Error(`Failed to create directory ${dir}: ${err.message || err}`)
    }
  }
}
