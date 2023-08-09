import express, {response} from "express";
import rssClient from "./rssClient.js";
import {responseWithError, responseWithSuccess} from "../utils.js";
import RssParser from "rss-parser";
import prisma from "../dbClient.js";

const rssParser = new RssParser()
const router = express.Router();

router.get('/subscription', async (req, res) => {
    try {
        const page = req.query.page || 1
        const size = req.query.size || 10
        if (page <= 0 || size <= 0) {
            throw '[page]和[size]参数不能小于等于0'
        }
        const results = await prisma.rssSubscription.findMany({
            take: Number(size),
            skip: Number(page - 1) * Number(size)
        })
        responseWithSuccess(res, null, results)
    } catch (error) {
        responseWithError(res, error);
    }
});

router.post('/subscription', async (req, res) => {
    try {
        const {link} = req.body
        console.info('link:', link)
        if (!link) {
            throw '参数[link]为空'
        }
        // 下载订阅
        const response = await rssClient.fetchRss(link)
        // 解析订阅
        const feed = await rssParser.parseString(response.data)
        if (!feed || !feed.link) {
            throw 'feed解析失败：' + feed
        }
        // 根据link去查询是否存在
        let exist = await prisma.rssSubscription.findUnique({
            where: {
                link: feed.link
            }
        })
        if (!exist) {
            // 生成数据
            exist = await prisma.rssSubscription.create({
                data: {
                    link: feed.link,
                    title: feed.title,
                    description: feed.description,
                    ttl: Number(feed.ttl),
                    lastFetchAt: new Date()
                },
            })
        }
        responseWithSuccess(res, null, exist)
    } catch (error) {
        responseWithError(res, error);
    }
})

router.get('/download', (req, res) => {
    rssClient.downloadTorrentInFile('http://127.0.0.1/tmp.torrent', '/tmp/tmp.torrent', res)
})

export default router;
