import 'module-alias/register'

import MovieDbTool from '@server/anime/tools/MovieDbTool'
import FileParseTool from '@server/anime/tools/FileParseTool'
const movieDbTool = new MovieDbTool()
const fileParseTool = new FileParseTool()
const file =
  '/tmp/anime/[新Sub][10月新番][堤亚穆帝国物语~从断头台开始 公主重生后的逆转人生~][01~12][HEVC][10bit][1080p][简日双语].torrent'
console.log(fileParseTool.parseFileName(file).animeTitle)
