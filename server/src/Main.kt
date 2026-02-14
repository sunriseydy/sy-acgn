package dev.sunriseydy.acgn.server

import io.github.cdimascio.dotenv.dotenv

fun main(args: Array<String>) {
    // 加载 .env 文件，如果文件不存在（例如在生产环境），则忽略错误
    val dotenv = dotenv {
         ignoreIfMissing = true
    }
    // 将 .env 中的变量写入 System Properties
    // 这样 Ktor 的 HOCON/YAML 配置文件可以通过 ${VAR_NAME} 获取到这些值
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    io.ktor.server.cio.EngineMain.main(args)
}
