import express from "express";
import rssHttp from "../rssHttp.js";
import {responseWithError} from "../../utils.js";

const router = express.Router();

router.get('/', (req, res) => {
    rssHttp.get('http://127.0.0.1/rss.xml')
        .then(response => {
            // console.log(r.data);
            res.send(response.data);
        })
        .catch(error => {
            responseWithError(res, error);
        })
});

export default router;
