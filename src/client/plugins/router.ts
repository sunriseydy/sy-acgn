import { createRouter, createWebHashHistory } from 'vue-router'
import anime from '@client/routes/anime'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [...anime],
})
export default router
