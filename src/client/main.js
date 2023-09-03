import { createApp } from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify.js'
import router from './plugins/router.js'
import http from '@/client/plugins/axios.js'
import { toast } from 'vuetify-sonner'
import VuetifyUseDialog from 'vuetify-use-dialog'

// 设置响应拦截器
http.interceptors.response.use(
  (response) => {
    // 2xx 范围内的状态码都会触发该函数。
    console.info(response)
    if (response && response.data && response.data.success) {
      return response
    }
    if (response && response.data && !response.data.success) {
      return Promise.reject(response.data.message)
    }
  },
  (error) => {
    // 超出 2xx 范围的状态码都会触发该函数。
    console.log(error.toJSON())
    if (error.response) {
      // 请求成功发出且服务器也响应了状态码，但状态代码超出了 2xx 的范围
      toast('Error', {
        description: `${error.response.data.message || '请求失败'}`,
        cardProps: {
          color: 'error',
        },
      })
    } else if (error.request) {
      // 请求已经成功发起，但没有收到响应
      // `error.request` 在浏览器中是 XMLHttpRequest 的实例，
      // 而在node.js中是 http.ClientRequest 的实例
      toast('Error', {
        description: `${error.message || '请求失败'}`,
        cardProps: {
          color: 'error',
        },
      })
    } else {
      // 发送请求时出了点问题
      toast('Error', {
        description: `${error.message || '请求失败'}`,
        cardProps: {
          color: 'error',
        },
      })
    }
    return Promise.reject(error)
  },
)

createApp(App).use(router).use(vuetify).use(VuetifyUseDialog).mount('#app')
