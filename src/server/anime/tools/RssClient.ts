import axios from 'axios'
import fs from 'fs'
import { Response } from 'express'
import { responseWithError, responseWithSuccess } from '@server/utils/response'

const rssHttp = axios.create({
  timeout: 5000,
  responseType: 'text',
})

export default class RssClient {
  async downloadTorrentInFile(link: string, path: string, res: Response): Promise<any> {
    try {
      const response = await rssHttp.get(link, { responseType: 'stream' })
      const writer = fs.createWriteStream(path)
      response.data.pipe(writer)
      responseWithSuccess(res, '下载文件成功：' + path)
    } catch (error) {
      responseWithError(res, error)
    }
  }

  async fetchRss(link: string): Promise<any> {
    return rssHttp.get(link)
  }
}
