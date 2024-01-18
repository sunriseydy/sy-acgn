require('module-alias/register')
import express from 'express'
import ViteExpress from 'vite-express'
import rssApi from './rss/rssApi'
import cors from 'cors'

const app = express()
app.use(express.json())
app.use(cors())

app.use('/rss', rssApi)
app.get('/', (_, res) => res.json('Hello from express!'))

ViteExpress.listen(app, 9390, () =>
  console.log(`
🚀 Server ready at: http://localhost:9390
⭐️ See sample requests: http://pris.ly/e/js/rest-express#3-using-the-rest-api`),
)
