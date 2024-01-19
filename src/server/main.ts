import 'module-alias/register'

import { logRequest } from '@server/utils/request'
import 'reflect-metadata'
import express from 'express'
// @ts-ignore
import {
  ExpressErrorMiddlewareInterface,
  ExpressMiddlewareInterface,
  Middleware,
  useExpressServer,
} from 'routing-controllers'
import { Result } from '@server/utils/response'

const app = express()

// 请求日志中间件
@Middleware({ type: 'before' })
export class LoggingMiddleware implements ExpressMiddlewareInterface {
  use(request: any, response: any, next: (err: any) => any): void {
    logRequest(request)
    next(null)
  }
}

// 统一异常处理中间件
@Middleware({ type: 'after' })
class CustomErrorHandler implements ExpressErrorMiddlewareInterface {
  error(error: any, request: any, response: any, next: (err: any) => any) {
    console.log('request error:', error)
    response.status(500).json(Result.error(error))
  }
}

useExpressServer(app, {
  routePrefix: '/api',
  controllers: [`${__dirname}/**/controllers/*Controller.ts`],
  middlewares: [LoggingMiddleware, CustomErrorHandler],
  defaultErrorHandler: false,
})

// app.use('/rss', rssApi)
app.get('/', (_, res) => res.json('Hello from express!'))

const port: number = 9390
app.listen(port, () => console.log(`🚀 Server ready at: http://localhost:${port}`))
