package dev.sunriseydy.acgn.server

import io.github.cdimascio.dotenv.dotenv

/**
 * 服务器入口点
 *
 * 负责加载环境变量并启动 Ktor CIO 引擎。
 */
fun main(args: Array<String>) {
    // 加载 .env 文件，如果文件不存在（例如在生产环境），则忽略错误
    // 这允许在开发环境中使用 .env 文件配置，而在生产环境中使用系统环境变量
    val dotenv = dotenv {
         ignoreIfMissing = true
    }
    // 将 .env 中的变量写入 System Properties
    // 这样 Ktor 的 HOCON/YAML 配置文件可以通过 ${VAR_NAME} 获取到这些值
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    // 启动 CIO 引擎
    io.ktor.server.cio.EngineMain.main(args)
}
