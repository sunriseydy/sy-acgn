import express from "express";
import rssHttp from "../rssHttp.js";

const router = express.Router();

router.get('/', (req, res) => {
    rssHttp.get('http://127.0.0.1/rss.xml')
        .then((r) => {
            // console.log(r.data);
            res.send(r.data);
        })
});

export default router;
