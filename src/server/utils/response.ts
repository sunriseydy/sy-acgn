import { Response } from 'express'

export const responseWithError = (res: Response, error?: any) => {
  res.status(500).json({
    success: false,
    message: error.message || error || '失败',
    data: error,
  })
}

export const responseWithSuccess = (res: Response, message?: any, data?: any) => {
  res.status(200).json({
    success: true,
    message: message || '成功',
    data: data,
  })
}

export class Result {
  success: boolean
  message: string
  data?: any

  constructor(success: boolean, message: string, data?: any) {
    this.success = success
    this.message = message
    this.data = data
  }

  static ok(data?: any) {
    return new Result(true, '成功', data)
  }

  static error(error: any = '失败') {
    return new Result(false, error.message || error, null)
  }
}
