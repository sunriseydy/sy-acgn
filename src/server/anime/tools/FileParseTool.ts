import fs from 'fs/promises' // 使用fs的Promise版本
import { existsSync, statSync } from 'fs'
import parseFileName from './anime-file-parser/fileParser'

export default class FileParseTool {
  static VIDEO_EXTENSIONS = new Set(['mp4', 'mkv'])
  static SUBTITLE_EXTENSIONS = new Set(['srt', 'ass'])
  static TORRENT_EXTENSIONS = new Set(['torrent'])

  readonly parseFileName = parseFileName

  async getFileInDirectory(dir: string): Promise<string[]> {
    try {
      // 直接返回Promise，避免手动创建
      return fs.readdir(dir)
    } catch (err) {
      // 更现代的错误处理方式
      throw new Error(`Unable to read directory: ${dir}`)
    }
  }

  private static isExtensionMatch(fileName: string, extensions: Set<string>): boolean {
    const extension = fileName.split('.').pop()?.toLowerCase() || ''
    return extensions.has(extension)
  }

  isVideoFile(fileName: string): boolean {
    return FileParseTool.isExtensionMatch(fileName, FileParseTool.VIDEO_EXTENSIONS)
  }

  isSubtitleFile(fileName: string): boolean {
    return FileParseTool.isExtensionMatch(fileName, FileParseTool.SUBTITLE_EXTENSIONS)
  }

  isTorrentFile(fileName: string): boolean {
    return FileParseTool.isExtensionMatch(fileName, FileParseTool.TORRENT_EXTENSIONS)
  }

  hasVideoFile(files: string[]): boolean {
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
}
