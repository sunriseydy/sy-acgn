import http from '@/plugins/axios'

const rssApi = {
  // 查询 rss 订阅
  async queryRssSubscription(page: number, size: number) {
    return http.get('/anime/rss/subscription', {
      params: {
        page: page,
        size: size,
      },
    })
  },
  // 新增 rss 订阅
  async addRssSubscription(link: string) {
    return http.post('/anime/rss/subscription', {
      link: link,
    })
  },
  // 修改 rss 订阅
  async editRssSubscription(rssSubscriptionId: number, title?: string, ttl?: number) {
    return http.put(`/anime/rss/subscription/${rssSubscriptionId}`, {
      title: title,
      ttl: ttl,
    })
  },
  // 删除 rss 订阅
  async deleteRssSubscription(rssSubscriptionId: number) {
    return http.delete(`/anime/rss/subscription/${rssSubscriptionId}`)
  },
  // 标记订阅为已读
  async markRssSubscriptionRead(rssSubscriptionId: number) {
    return http.put(`/anime/rss/subscription/${rssSubscriptionId}/read`)
  },
  // 查询 rss 订阅的条目
  async queryRssSubscriptionItem(rssSubscriptionId: number, page: number, size: number) {
    return http.get(`/anime/rss/subscription/${rssSubscriptionId}/item`, {
      params: {
        page: page,
        size: size,
      },
    })
  },
  // 将订阅内容标记为已读
  async markRssSubscriptionItemRead(rssSubscriptionId: number, rssSubscriptionItemId: number) {
    return http.put(
      `/anime/rss/subscription/${rssSubscriptionId}/item/${rssSubscriptionItemId}/read`,
    )
  },
  // 更新订阅内容
  async updateRssSubscriptionItem(rssSubscriptionId: number) {
    return http.post(`/anime/rss/subscription/${rssSubscriptionId}/item`)
  },
}

export default rssApi
