package dev.sunriseydy.acgn.client

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/**
 * WebAssembly (Wasm-JS) 网页端应用入口点
 *
 * 将 Compose Multiplatform 界面挂载到 HTML document.body 节点。
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return
    ComposeViewport(body) {
        App()
    }
}
