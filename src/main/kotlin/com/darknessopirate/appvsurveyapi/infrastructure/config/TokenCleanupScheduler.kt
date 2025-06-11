package com.darknessopirate.appvsurveyapi.infrastructure.config


import com.darknessopirate.appvsurveyapi.infrastructure.service.AuthServiceImpl
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class TokenCleanupScheduler(
    private val authService: AuthServiceImpl
) {

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    fun cleanupExpiredTokens() {
        authService.cleanupExpiredTokens()
        println("Expired refresh tokens cleaned up")
    }
}