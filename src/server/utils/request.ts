import { Request } from 'express'

export const parsePageParams = (req: Request) => {
  const page = req.query.page === '0' ? 0 : req.query.page || 1
  const size = req.query.size === '0' ? 0 : req.query.size || 10
  return {
    take: page === 0 ? undefined : Number(size),
    skip: page === 0 ? undefined : (Number(page) - 1) * Number(size),
  }
}

export const logRequest = (request: Request) => {
  console.log(request.method, request.originalUrl, request.query || {}, request.body || {})
}

export class PageParams {
  take?: number
  skip?: number
  constructor(page: number = 0, size: number = 10) {
    this.take = page === -1 ? undefined : size
    this.skip = page === -1 ? undefined : page * size
  }
}
