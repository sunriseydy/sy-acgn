import axios from 'axios';
import fs from "fs";
import {responseWithError, responseWithSuccess} from "../utils.js";

const rssHttp = axios.create({
    timeout: 5000,
    responseType: 'text',
});

const downloadTorrentInFile = async (link, path, res) => {
    try {
        const response = await rssHttp.get(link, {responseType: 'stream'})
        const writer = fs.createWriteStream(path);
        response.data.pipe(writer)
        responseWithSuccess(res, '下载文件成功：' + path)
    } catch (error) {
        responseWithError(res, error)
    }
}

const fetchRss = (link) => {
    return rssHttp.get(link)
}

export default {fetchRss, downloadTorrentInFile};