package com.darknessopirate.appvsurveyapi.infrastructure.config

import com.darknessopirate.appvsurveyapi.infrastructure.service.AuthServiceImpl
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdminUserInitializer {

    @Bean
    fun initializeAdminUser(authService: AuthServiceImpl) = ApplicationRunner {
        authService.createAdminUserIfNotExists()
        println("Admin user initialization completed.")
    }
}