import { Response, Request } from 'express'

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
