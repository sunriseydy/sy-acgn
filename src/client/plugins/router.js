import { createRouter, createWebHashHistory } from 'vue-router'
import rss from '@/client/routes/rss.js'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [...rss],
})
export default router
