package dev.sunriseydy.acgn.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 *@author SunriseYDY
 *@date 2024-05-27 17:59
 */
@RestController
class HelloWorld {
    @GetMapping("/hello-world")
    fun helloWorld(): Map<String, String> = mapOf("message" to "Hello World!")
}