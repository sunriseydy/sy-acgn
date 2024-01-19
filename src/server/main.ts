require('module-alias/register')
import 'reflect-metadata'
import express from 'express'
// @ts-ignore
import { useExpressServer } from 'routing-controllers'

const app = express()

useExpressServer(app, {
  routePrefix: '/api',
  controllers: [`${__dirname}/**/controllers/*Controller.ts`],
})

// app.use('/rss', rssApi)
app.get('/', (_, res) => res.json('Hello from express!'))

const port: number = 9390
app.listen(port, () => console.log(`🚀 Server ready at: http://localhost:${port}`))
