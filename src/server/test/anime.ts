import 'module-alias/register'

import MovieDbTool from '@server/anime/tools/MovieDbTool'
const movieDbTool = new MovieDbTool()
movieDbTool.getFirstSearchResultOfTv('宇崎ちゃんは遊びたい！ ω').then((res) => {
  console.log(res)
})
