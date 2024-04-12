import { ShowResponse, TvResult } from 'moviedb-promise'

interface AnimeInfo {
  animeTitle: string
  animeYear: number | null
  season: number | null
  episode: number
  extensionName: ExtensionName
  fileName: string
  groups: Group[]
  videoSource: string[]
  videoQuality: VideoQuality[]
  videoSubtitle: string[]
  otherInfo: string[]
  tagedName: string[] | Group[] | VideoQuality[] | Subtitle[]
  noBrowser: boolean
  tvResults: TvResult[]
  tvDetail: ShowResponse
}

interface ExtensionName {
  result: string
  type: string
  raw: string
  trueName: string
}

interface Group {
  result: string
  raw: string
  type: string
}

interface VideoQuality {
  result: string
  raw: string
  type: string
}

interface Subtitle {
  result: string
  raw: string
  type: string
}

export type { AnimeInfo, ExtensionName, Group, VideoQuality, Subtitle }
