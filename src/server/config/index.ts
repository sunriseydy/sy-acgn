import { AxiosProxyConfig } from 'axios'

interface NetworkProxyConfig extends AxiosProxyConfig {
  enabled: boolean
}

function getEnvValue(name: string) {
  return process.env[name] || ''
}

const config = {
  network: {
    proxy: {
      enabled: getEnvValue('PROXY_ENABLED') === 'true' || false,
      host: getEnvValue('PROXY_HOST'),
      port: getEnvValue('PROXY_PORT') || 0,
      protocol: getEnvValue('PROXY_PROTOCOL'),
      auth: getEnvValue('PROXY_USERNAME') &&
        getEnvValue('PROXY_PASSWORD') && {
          username: getEnvValue('PROXY_USERNAME'),
          password: getEnvValue('PROXY_PASSWORD'),
        },
    } as NetworkProxyConfig,
  },
  integration: {
    tmdbKey: getEnvValue('TMDB_KEY'),
  },
  anime: {
    path: {
      downloadPath: getEnvValue('DOWNLOAD_PATH'),
      targetPath: getEnvValue('TARGET_PATH'),
    },
  },
}
export default config
