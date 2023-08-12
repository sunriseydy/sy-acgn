import express from 'express'
import rssClient from './rssClient.js'
import { responseWithError, responseWithSuccess } from '../utils.js'
import RssParser from 'rss-parser'
import prisma from '../dbClient.js'

const rssParser = new RssParser()
const router = express.Router()

const rssSubscriptionPath = '/subscription'
const rssSubscriptionItemPath = `${rssSubscriptionPath}/:rssSubscriptionId/item`

/**
 * 查询 rss 订阅
 */
router.get(rssSubscriptionPath, async (req, res) => {
  try {
    const page = req.query.page || 1
    const size = req.query.size || 10
    if (page <= 0 || size <= 0) {
      throw '[page]和[size]参数不能小于等于0'
    }
    const results = await prisma.rssSubscription.findMany({
      take: Number(size),
      skip: Number(page - 1) * Number(size),
    })
    responseWithSuccess(res, null, results)
  } catch (error) {
    responseWithError(res, error)
  }
})

/**
 * 新增 rss 订阅
 */
router.post(rssSubscriptionPath, async (req, res) => {
  try {
    const { link } = req.body
    console.info('link:', link)
    if (!link) {
      throw '参数[link]为空'
    }
    // 根据link去查询是否存在
    let exist = await prisma.rssSubscription.findUnique({
      where: {
        link: link,
      },
    })
    if (exist) {
      throw `该订阅[%{link}]已存在`
    }
    // 下载订阅
    const response = await rssClient.fetchRss(link)
    // 解析订阅
    const feed = await rssParser.parseString(response.data)
    if (!feed || !feed.link) {
      throw 'feed解析失败：' + feed
    }
    // 生成数据
    exist = await prisma.rssSubscription.create({
      data: {
        link: link,
        title: feed.title,
        description: feed.description,
        ttl: Number(feed.ttl),
        lastFetchAt: new Date(),
      },
    })
    responseWithSuccess(res, null, exist)
  } catch (error) {
    responseWithError(res, error)
  }
})

/**
 * 修改 rss 订阅
 */
router.put(`${rssSubscriptionPath}/:id`, async (req, res) => {
  try {
    const { id } = req.params
    const { title, ttl } = req.body
    if (!id) {
      throw '[id]参数不存在'
    }
    // 先查询数据是否存在
    const exist = await prisma.rssSubscription.findUnique({
      where: { id: Number(id) },
    })
    if (!exist) {
      throw `该数据[${id}]不存在`
    }
    const result = await prisma.rssSubscription.update({
      where: {
        id: Number(id),
      },
      data: {
        title: title || undefined,
        ttl: ttl ? Number(ttl) : undefined,
      },
    })
    responseWithSuccess(res, null, result)
  } catch (error) {
    responseWithError(res, error)
  }
})

/**
 * 删除 rss 订阅
 */
router.delete(`${rssSubscriptionPath}/:id`, async (req, res) => {
  try {
    const { id } = req.params
    if (!id) {
      throw '[id]参数不存在'
    }
    // 先查询数据是否存在
    const exist = await prisma.rssSubscription.findUnique({
      where: { id: Number(id) },
    })
    if (!exist) {
      throw `该数据[${id}]不存在`
    }
    prisma.$transaction(async (tx) => {
      // 删除订阅内容
      await tx.$executeRaw`delete from main.RssSubscriptionItem where rssSubscriptionId = ${Number(
        id,
      )};`
      // 删除订阅
      await tx.$executeRaw`delete from main.RssSubscription where id = ${Number(id)};`
    })
    responseWithSuccess(res)
  } catch (error) {
    responseWithError(res, error)
  }
})

/**
 * 获取订阅内容
 */
router.get(rssSubscriptionItemPath, async (req, res) => {
  try {
    const { rssSubscriptionId } = req.params
    if (!rssSubscriptionId) {
      throw '[rssSubscriptionId]参数不存在'
    }
    const results = await prisma.rssSubscriptionItem.findMany({
      where: {
        rssSubscriptionId: Number(rssSubscriptionId),
      },
      orderBy: [
        {
          pubDate: 'desc',
        },
      ],
    })
    responseWithSuccess(res, null, results)
  } catch (error) {
    responseWithError(res, error)
  }
})

/**
 * 更新订阅内容
 */
router.post(rssSubscriptionItemPath, async (req, res) => {
  try {
    const { rssSubscriptionId } = req.params
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
    const response = await rssClient.fetchRss(link)
    // 解析订阅
    const feed = await rssParser.parseString(response.data)
    if (!feed || !feed.link) {
      throw 'feed解析失败：' + feed
    }
    const items = feed.items.filter((item) => {
      return (
        item.guid &&
        item.link &&
        item.title &&
        item.enclosure.type === 'application/x-bittorrent' &&
        item.enclosure.url
      )
    })
    const insertItems = []
    for (const item of items) {
      // 先查询该内容是否存在
      const exist = await prisma.rssSubscriptionItem.findUnique({
        where: {
          rssSubscriptionId_guid: {
            rssSubscriptionId: Number(rssSubscriptionId),
            guid: item.guid,
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
          rssSubscriptionId: Number(rssSubscriptionId),
          isRead: false,
          torrentLink: item.enclosure.url,
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
    responseWithSuccess(res)
  } catch (error) {
    responseWithError(res, error)
  }
})

/**
 * 将订阅内容标记为已读
 */
router.put(`${rssSubscriptionItemPath}/:uuid/read`, async (req, res) => {
  try {
    const { rssSubscriptionId, uuid } = req.params
    if (!rssSubscriptionId) {
      throw '[rssSubscriptionId]参数不存在'
    }
    if (!uuid) {
      throw '[uuid]参数不存在'
    }
    const result =
      await prisma.$executeRaw`update RssSubscriptionItem set isRead = true, updatedAt = current_timestamp where id = ${uuid}`
    responseWithSuccess(res, null, result)
  } catch (error) {
    responseWithError(res, error)
  }
})

export default router
