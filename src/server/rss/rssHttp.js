import axios from 'axios';
import fs from "fs";
import {responseWithError} from "../utils.js";

const rssHttp = axios.create({
    timeout: 5000,
    responseType: 'text',
});

const downloadTorrentInFile = (url, path, res) => {
    try {
        rssHttp.get(url, { responseType: 'stream' })
            .then(response => {
                const writer = fs.createWriteStream(path);
                response.data.pipe(writer)
                res.json('下载文件成功：' + path)
            })
            .catch(error => {
                responseWithError(res, error)
            })
    } catch (error) {
        responseWithError(res, error)
    }
};

export default {rssHttp, downloadTorrentInFile};