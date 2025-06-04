package com.darknessopirate.appvsurveyapi.api.config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    // Swagger UI and API docs
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()
                    // Your API endpoints for development
                    .requestMatchers("/api/**").permitAll()
                    .anyRequest().authenticated()
            }
            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) } // Add this line
            .httpBasic(Customizer.withDefaults())

        return http.build()
    }

    // Add this method to define CORS configuration
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // Allow specific origins
        configuration.allowedOrigins = listOf(
            "http://localhost:4200",    // Your Angular dev server
            "http://127.0.0.1:4200"     // Alternative localhost
        )

        // Allow specific methods
        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        )

        // Allow specific headers
        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        )

        // Allow credentials
        configuration.allowCredentials = true

        // Set max age for preflight requests
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", configuration)

        return source
    }
}