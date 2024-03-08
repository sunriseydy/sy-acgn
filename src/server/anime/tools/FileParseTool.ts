import fs from 'fs'

export default class FileParseTool {
  static VIDEO_EXTENSIONS = ['mp4', 'mkv']
  static SUBTITLE_EXTENSIONS = ['srt', 'ass']

  async getFileInDirectory(dir: string): Promise<any> {
    return new Promise((resolve, reject) => {
      fs.readdir(dir, (err, files) => {
        if (err) {
          reject(err)
        } else {
          resolve(files)
        }
      })
    })
  }

  isVideoFile(filename: string): boolean {
    return FileParseTool.VIDEO_EXTENSIONS.includes(
      (filename.split('.').pop() as string).toLowerCase(),
    )
  }

  isSubtitleFile(filename: string): boolean {
    return FileParseTool.SUBTITLE_EXTENSIONS.includes(
      (filename.split('.').pop() as string).toLowerCase(),
    )
  }

  hasVideoFile(files: string[]): boolean {
    return files.some((file) => this.isVideoFile(file))
  }
}
