import 'module-alias/register'
// @ts-ignore
import sleep from 'sleep'

import MovieDbTool from '@server/anime/tools/MovieDbTool'
import FileParseTool from '@server/anime/tools/FileParseTool'
const movieDbTool = new MovieDbTool()
const fileParseTool = new FileParseTool()
const dir = '/tmp/Anime/'
fileParseTool.getFileInDirectory(dir).then(async (files) => {
  for (const file of files) {
    console.log(file)
    const animeInfo = fileParseTool.parseFileName(file)
    if (animeInfo && animeInfo.animeTitle) {
      sleep.sleep(1)
      console.log(animeInfo.animeTitle)
      const results = await movieDbTool.searchAnimeTv(animeInfo.animeTitle)
      if (results[0]) {
        if (results.length > 1) {
          console.log('多个结果:', results)
          continue
        }
        const result = results[0]
        console.log(result.name)
        if (result.name) {
          fileParseTool.moveFile(`${dir}/${file}`, `/tmp/anime/${result.name}_${file}`)
        }
      }
    }
  }
})
// const file = '[VCB-Studio] Tate no Yuusha no Nariagari.torrent'
// console.log(fileParseTool.parseFileName(file))
