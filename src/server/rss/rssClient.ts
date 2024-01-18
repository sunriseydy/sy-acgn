import axios from 'axios'
import fs from 'fs'
import { Response } from 'express'
import { responseWithError, responseWithSuccess } from '../utils'

const rssHttp = axios.create({
  timeout: 5000,
  responseType: 'text',
})

const downloadTorrentInFile = async (link: string, path: string, res: Response) => {
  try {
    const response = await rssHttp.get(link, { responseType: 'stream' })
    const writer = fs.createWriteStream(path)
    response.data.pipe(writer)
    responseWithSuccess(res, '下载文件成功：' + path)
  } catch (error) {
    responseWithError(res, error)
  }
}

const fetchRss = (link: string) => {
  return rssHttp.get(link)
}

export default { fetchRss, downloadTorrentInFile }
