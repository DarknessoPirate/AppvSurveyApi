package com.darknessopirate.appvsurveyapi.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .servers(listOf(
                Server().url("http://localhost:8080").description("Development server")
            ))
    }

    private fun apiInfo(): Info {
        return Info()
            .title("Survey API")
            .description("API for managing surveys, questions, and responses")
            .version("1.0")
            .license(License().name("MIT License").url("https://opensource.org/licenses/MIT"))
    }
}