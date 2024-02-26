import { PageParams } from '@server/utils/request'
import prisma from '@server/DbClient'
import RssClient from '@server/anime/tools/RssClient'
import RssParser from 'rss-parser'

export default class RssService {
  private rssParser: RssParser = new RssParser()
  private rssClient: RssClient = new RssClient()

  /**
   * 查询 rss 订阅
   * @param pageParams
   */
  getRssSubscribeList(pageParams: PageParams): Promise<any> {
    return prisma.rssSubscription.findMany(pageParams)
  }

  /**
   * 新增 rss 订阅
   */
  async createRssSubscribe(link: string): Promise<any> {
    if (!link) {
      throw '参数[link]为空'
    }
    // 根据link去查询是否存在
    const exist = await prisma.rssSubscription.findUnique({
      where: {
        link: link,
      },
    })
    if (exist) {
      throw `该订阅[${link}]已存在`
    }
    // 下载订阅
    const response = await this.rssClient.fetchRss(link)
    // 解析订阅
    const feed = await this.rssParser.parseString(response.data)
    if (!feed || !feed.link) {
      throw 'feed解析失败：' + feed
    }
    // 生成数据
    return prisma.rssSubscription.create({
      data: {
        link: link,
        title: feed.title || link,
        description: feed.description,
        ttl: Number(feed.ttl),
        lastFetchAt: new Date(),
      },
    })
  }

  /**
   * 修改 rss 订阅
   */
  async updateRssSubscribe(id: number, title?: string, ttl?: number): Promise<any> {
    if (!id) {
      throw '[id]参数不存在'
    }
    // 先查询数据是否存在
    const exist = await prisma.rssSubscription.findUnique({
      where: { id },
    })
    if (!exist) {
      throw `该数据[${id}]不存在`
    }
    return prisma.rssSubscription.update({
      where: {
        id,
      },
      data: {
        title: title || exist.link,
        ttl: ttl ? ttl : undefined,
      },
    })
  }

  /**
   * 删除 rss 订阅
   * @param id
   */
  async deleteRssSubscribe(id: number): Promise<any> {
    if (!id) {
      throw '[id]参数不存在'
    }
    // 先查询数据是否存在
    const exist = await prisma.rssSubscription.findUnique({
      where: { id },
    })
    if (!exist) {
      throw `该数据[${id}]不存在`
    }
    prisma.$transaction(async (tx) => {
      // 删除订阅内容
      await tx.$executeRaw`delete from main.RssSubscriptionItem where rssSubscriptionId = ${id};`
      // 删除订阅
      await tx.$executeRaw`delete from main.RssSubscription where id = ${id};`
    })
  }

  /**
   * 标记订阅为已读
   */
  markRssSubscribeAsRead(id: number): any {
    if (!id) {
      throw '[id]参数不存在'
    }
    return prisma.$executeRaw`update RssSubscriptionItem set isRead = true, updatedAt = current_timestamp where rssSubscriptionId = ${id} and isRead = false`
  }

  /**
   * 获取订阅内容
   */
  getRssSubscribeItemList(rssSubscriptionId: number, pageParams: PageParams): any {
    if (!rssSubscriptionId && rssSubscriptionId !== 0) {
      throw '[id]参数不存在'
    }
    return prisma.rssSubscriptionItem.findMany({
      select: {
        id: true,
        title: true,
        link: true,
        pubDate: true,
        isRead: true,
        torrentLink: true,
      },
      where: {
        rssSubscriptionId: rssSubscriptionId === 0 ? undefined : rssSubscriptionId,
        isRead: rssSubscriptionId === 0 ? false : undefined,
      },
      orderBy: [
        {
          pubDate: 'desc',
        },
      ],
      take: pageParams.take,
      skip: pageParams.skip,
    })
  }

  /**
   * 获取订阅内容更新
   */
  async fetchRssSubscribeItem(rssSubscriptionId: number): Promise<any> {
    if (!rssSubscriptionId) {
      throw '[rssSubscriptionId]参数不存在'
    }
    // 先查询数据是否存在
    const exist = await prisma.rssSubscription.findUnique({
      where: { id: Number(rssSubscriptionId) },
    })
    if (!exist) {
      throw `该数据[${rssSubscriptionId}]不存在`
    }
    const link = exist.link
    // 下载订阅
    const response = await this.rssClient.fetchRss(link)
    // 解析订阅
    const feed = await this.rssParser.parseString(response.data)
    if (!feed || !feed.link) {
      throw 'feed解析失败：' + feed
    }
    const items = feed.items.filter((item) => {
      return (
        item.guid &&
        item.link &&
        item.title &&
        item.enclosure?.type === 'application/x-bittorrent' &&
        item.enclosure?.url
      )
    })
    const insertItems: any[] = []
    for (const item of items) {
      // 先查询该内容是否存在
      const exist = await prisma.rssSubscriptionItem.findUnique({
        where: {
          rssSubscriptionId_guid: {
            rssSubscriptionId,
            guid: item.guid || '',
          },
        },
      })
      if (!exist) {
        insertItems.push({
          title: item.title,
          link: item.link,
          guid: item.guid,
          content: item.content,
          pubDate: item.isoDate,
          rssSubscriptionId,
          isRead: false,
          torrentLink: item.enclosure?.url,
          rawJson: JSON.stringify(item),
        })
      }
    }
    if (insertItems) {
      // 在事务中批量插入
      await prisma.$transaction(async (tx) => {
        for (const insertItem of insertItems) {
          await tx.rssSubscriptionItem.create({
            data: insertItem,
          })
        }
      })
    }
  }

  /**
   * 将订阅内容标记为已读
   */
  markRssSubscribeItemAsRead(rssSubscriptionId: number, uuid: string): any {
    if (!rssSubscriptionId) {
      throw '[rssSubscriptionId]参数不存在'
    }
    if (!uuid) {
      throw '[uuid]参数不存在'
    }
    return prisma.$executeRaw`update RssSubscriptionItem set isRead = true, updatedAt = current_timestamp where id = ${uuid}`
  }
}
