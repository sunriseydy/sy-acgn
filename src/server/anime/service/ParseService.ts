import FileParseTool from '@server/anime/tools/FileParseTool'
import MovieDbTool from '@server/anime/tools/MovieDbTool'
import { AnimeInfo } from '@server/anime/tools/anime-file-parser/AnimeInfo'

export default class ParseService {
  private fileParseTool = new FileParseTool()
  private movieDbTool = new MovieDbTool()

  async parseAnimeFile(path: string) {
    if (!this.fileParseTool.isFileExists(path)) {
      throw `【${path}】不存在`
    }
    if (await this.fileParseTool.isAllSeasons(path)) {
      // 是那种全季的
      throw `【${path}】下包含多个目录`
    }
    return this.parseAnimeFileName(path)
  }

  async parseAnimeFileName(filName: string) {
    if (!filName) {
      throw '参数【fileName】为空'
    }
    const animeInfo = this.fileParseTool.parseFileName(filName) || {}
    if (animeInfo.animeTitle) {
      // 根据名称查询 TMDB
      const tvResults = await this.movieDbTool.searchAnimeTv(animeInfo.animeTitle)
      animeInfo.tvResults = tvResults
    } else {
      throw `【${filName}】解析文件名称失败`
    }
    if (animeInfo.tvResults[0] && animeInfo.tvResults.length === 1) {
      // 如果只有一个搜索结果，则直接获取详情
      const { id } = animeInfo.tvResults[0]
      if (id) {
        const tvDetail = await this.movieDbTool.getTvDetail(id)
        animeInfo.tvDetail = tvDetail
      }
    }
    if (animeInfo.tvDetail && animeInfo.tvDetail.id) {
      // 如果 tv show 详情存在，则判断 seasons
      const {
        tvDetail: { seasons },
      } = animeInfo
      if (seasons) {
        if (seasons.length === 1) {
          // 只有一个 season
          const season = seasons[0]
          if (season && season.season_number != undefined) {
            // 直接覆盖文件名解析出来的 season
            animeInfo.season = season.season_number
          }
        }
        if (
          seasons.length === 2 &&
          seasons[0].season_number == 0 &&
          seasons[1].season_number == 1
        ) {
          // 如果有两个 season，一个是0一个是1,则赋值 season 为 1
          animeInfo.season = 1
        }
      }
    }
    return animeInfo
  }

  move(path: string, animeInfo: AnimeInfo) {
    if (!this.fileParseTool.isFileExists(path)) {
      throw '路径不存在'
    }
    if (!animeInfo.animeTitle) {
      throw '动画名称不存在'
    }
    if (!animeInfo.tvDetail || animeInfo.tvDetail.id === undefined) {
      throw '动画搜索结果不存在，请手动搜索动画'
    }
    if (animeInfo.season === null || animeInfo.season === undefined) {
      throw '动画的季数不存在，请手动设置季数'
    }
    if (this.fileParseTool.isFile(path)) {
      // 移动文件
    }
    if (this.fileParseTool.isDirectory(path)) {
      // 移动目录
    }
  }

  private async moveFile(path: string, animeInfo: AnimeInfo) {
    if (!this.fileParseTool.isVideoFile(path)) {
      throw '不是视频文件'
    }
    if (animeInfo.episode === null || animeInfo.episode === undefined) {
      throw '动画的集数不存在，请手动设置集数'
    }
    const dir = this.fileParseTool.generateAnimeDirectoryName(animeInfo)
    const fileName = this.fileParseTool.generateAnimeFileName(path, animeInfo)
    await this.fileParseTool.createDirectory(dir)
    await this.fileParseTool.moveFile(path, `${dir}${fileName}`)
  }

  private async moveDirectory(path: string, animeInfo: AnimeInfo) {
    // 处理路径
    const dirname = this.fileParseTool.getDirNameFromPath(path)
    if (await this.fileParseTool.isAllSeasons(dirname)) {
      // 是那种全季的
      throw '包含多个目录'
    }
    const videos = await this.fileParseTool.getAllVideoFile(dirname)
    const subtitles = await this.fileParseTool.getAllSubtitleFile(dirname)
    const hasSubtitles = subtitles && subtitles.length !== 0
    if (hasSubtitles && subtitles.length !== videos.length) {
      throw '目录中视频文件数量和字母文件数量不匹配'
    }
    const {
      tvDetail: { seasons },
    } = animeInfo
    if (!seasons) {
      throw 'TMDB数据中缺少季数据'
    }
    const seasonData = seasons.find((season) => {
      season.season_number === animeInfo.season
    })
    if (!seasonData) {
      throw 'TMDB数据中缺少对应的季数据:' + animeInfo.season
    }
    if (!seasonData.episode_count) {
      throw 'TMDB数据中缺少对应的季数据的集数据'
    }
    if (seasonData.episode_count !== videos.length) {
      throw 'TMDB数据中季数据的集数量和目录中视频数量不匹配'
    }
    // 处理含有视频文件的子目录
    const directories = (await this.fileParseTool.getAllDirectory(dirname)).filter((dir) =>
      this.fileParseTool.hasVideoFile(dir),
    )
    for (const dir of directories) {
      const dirName = this.fileParseTool.getFileNameFromPath(dir)
      await this.fileParseTool.moveFile(dir, `${dirname}.${dirName}`)
    }
    // 生成视频文件名并移动
    const videoFileNames = this.fileParseTool.generateVideoFileNameByFileNameSort(videos, animeInfo)
    await this.fileParseTool.moveFiles(videos, videoFileNames, dirname)
    if (hasSubtitles) {
      // 生成字幕文件名并移动
      const subtitleFileName = this.fileParseTool.generateSubtitleFileNameByFileNameSort(
        subtitles,
        videoFileNames,
      )
      await this.fileParseTool.moveFiles(subtitles, subtitleFileName, dirname)
    }
    // 移动目录
    const targetDir = this.fileParseTool.generateTargetDirectory(animeInfo)
    await this.fileParseTool.createDirectory(targetDir)
    await this.fileParseTool.moveFile(dirname, targetDir)
  }
}
