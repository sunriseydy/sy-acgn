import { AxiosProxyConfig } from 'axios'

interface NetworkProxyConfig extends AxiosProxyConfig {
  enabled: boolean
}

const config = {
  network: {
    proxy: {
      enabled: process.env.PROXY_ENABLED === 'true' || false,
      host: process.env.PROXY_HOST || '',
      port: process.env.PROXY_PORT || 0,
      protocol: process.env.PROXY_TYPE || '',
      auth: process.env.PROXY_USERNAME &&
        process.env.PROXY_PASSWORD && {
          username: process.env.PROXY_USERNAME || '',
          password: process.env.PROXY_PASSWORD || '',
        },
    } as NetworkProxyConfig,
  },
  anime: {
    tmdbKey: process.env.TMDB_KEY || '',
  },
}
export default config
