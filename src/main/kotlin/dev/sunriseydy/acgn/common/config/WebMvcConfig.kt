package dev.sunriseydy.acgn.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 *@author SunriseYDY
 *@date 2024-05-29 17:35
 */
@Configuration
@EnableWebMvc
class WebMvcConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .exposedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
}