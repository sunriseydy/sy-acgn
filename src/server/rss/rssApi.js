import express from "express";
import rssHttp from "./rssHttp.js";
import {responseWithError} from "../utils.js";
import RssParser from "rss-parser";
const rssParser = new RssParser()

const router = express.Router();

router.get('/', (req, res) => {
    rssHttp.rssHttp.get('http://127.0.0.1/rss.xml')
        .then(response => {
            // console.log(r.data);
            rssParser.parseString(response.data)
                .then(feed => {
                    res.json(feed);
                })
                .catch(error => {
                    responseWithError(res, error);
                })
        })
        .catch(error => {
            responseWithError(res, error);
        })
});

router.get('/download', (req, res) => {
    rssHttp.downloadTorrentInFile('http://127.0.0.1/tmp.torrent', '/tmp/tmp.torrent', res)
})

export default router;
