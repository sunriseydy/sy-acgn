import axios from 'axios'
// https://axios-http.com/zh/docs/req_config
const http = axios.create({
  timeout: 5000,
  baseURL: 'http://localhost:9390',
})

export default http
