package dev.sunriseydy.acgn.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

/**
 *@author SunriseYDY
 *@date 2024-05-31 18:36
 */
fun Application.configureVue() {
    routing {
        singlePageApplication {
            vue("vue-app/dist")
        }
    }
}