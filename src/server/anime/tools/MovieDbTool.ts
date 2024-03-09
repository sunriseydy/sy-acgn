import { MovieDb } from 'moviedb-promise'
import config from '@server/config'

export default class MovieDbTool {
  private readonly movieDb
  private readonly language = 'zh-CN'
  private readonly anime_genre_id = 16

  constructor(tmdbKey?: string) {
    this.movieDb = new MovieDb(tmdbKey || config.anime.tmdbKey || '')
  }

  private searchTv(query: string, page?: number) {
    return this.movieDb.searchTv(
      { query, page, language: this.language },
      { proxy: config.network.proxy.enabled && config.network.proxy },
    )
  }

  private searchMovie(query: string, page?: number) {
    return this.movieDb.searchMovie(
      { query, page, language: this.language },
      { proxy: config.network.proxy.enabled && config.network.proxy },
    )
  }

  async searchAnimeTv(query: string) {
    const result = await this.searchTv(query)
    if (result && result.total_results && result.results) {
      return result.results.filter(
        (item) =>
          item && item.name && item.genre_ids && item.genre_ids.includes(this.anime_genre_id),
      )
    } else {
      return []
    }
  }

  async getFirstSearchResultOfAnimeTv(query: string) {
    const results = await this.searchAnimeTv(query)
    return results[0]
  }

  async getFirstSearchResultOfMovie(query: string) {
    const result = await this.searchMovie(query)
    if (result && result.total_results && result.results && result.results[0]) {
      return result.results[0]
    } else {
      return null
    }
  }

  getTvDetail(id: number) {
    return this.movieDb.tvInfo(
      { id, language: this.language },
      { proxy: config.network.proxy.enabled && config.network.proxy },
    )
  }

  getMovieDetail(id: number) {
    return this.movieDb.movieInfo(
      { id, language: this.language },
      { proxy: config.network.proxy.enabled && config.network.proxy },
    )
  }
}
