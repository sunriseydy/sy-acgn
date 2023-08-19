import http from '@/client/plugins/axios.js'

// 调用服务端 src/server/rss/rssApi.js 文件中的接口
const rssApi = {
  // 查询 rss 订阅
  async queryRssSubscription(page, size) {
    return http.get('/rss/subscription', {
      params: {
        page: page,
        size: size,
      },
    })
  },
  // 新增 rss 订阅
  async addRssSubscription(link) {
    return http.post('/rss/subscription', {
      link: link,
    })
  },
  // 修改 rss 订阅
  async updateRssSubscription(rssSubscriptionId, title, ttl) {
    return http.put(`/rss/subscription/${rssSubscriptionId}`, {
      title: title,
      ttl: ttl,
    })
  },
  // 删除 rss 订阅
  async deleteRssSubscription(rssSubscriptionId) {
    return http.delete(`/rss/subscription/${rssSubscriptionId}`)
  },
  // 查询 rss 订阅的条目
  async queryRssSubscriptionItem(rssSubscriptionId, page, size) {
    return http.get(`/rss/subscription/${rssSubscriptionId}/item`, {
      params: {
        page: page,
        size: size,
      },
    })
  },
  // 将订阅内容标记为已读
  async markRssSubscriptionItemRead(rssSubscriptionId, rssSubscriptionItemId) {
    return http.put(`/rss/subscription/${rssSubscriptionId}/item/${rssSubscriptionItemId}/read`)
  },
}

export default rssApi
