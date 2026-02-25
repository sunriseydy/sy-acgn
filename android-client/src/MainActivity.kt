package dev.sunriseydy.acgn.client.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.sunriseydy.acgn.client.App
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        logger.info { "MainActivity onCreate" }
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
