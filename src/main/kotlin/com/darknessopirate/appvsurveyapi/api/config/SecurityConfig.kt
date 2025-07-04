package com.darknessopirate.appvsurveyapi.api.config

import com.darknessopirate.appvsurveyapi.infrastructure.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    // Swagger UI and API docs
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()

                    // Authentication endpoints
                    .requestMatchers("/api/auth/**").permitAll()

                    // Public survey endpoints (anonymous access for survey submission)
                    .requestMatchers("POST", "/api/summary/{id:\\d+}").permitAll() // get summary with password for anonymous users
                    .requestMatchers("POST", "/api/submissions/access-code").permitAll() // submit survey with access code for anonymous users
                    .requestMatchers("GET", "/api/surveys/access-code/**").permitAll() // get survey by access code for anonymous users

                    // All other API endpoints require ADMIN role
                    .requestMatchers("/api/**").hasRole("ADMIN")

                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // Allow specific origins
        configuration.allowedOrigins = listOf(
            "http://localhost:4200",    // Angular dev server
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