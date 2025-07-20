package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.request.user.LoginRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.user.LoginResponse
import com.darknessopirate.appvsurveyapi.domain.entity.user.RefreshToken
import com.darknessopirate.appvsurveyapi.domain.entity.user.User
import com.darknessopirate.appvsurveyapi.domain.repository.user.RefreshTokenRepository
import com.darknessopirate.appvsurveyapi.infrastructure.repository.UserRepository
import com.darknessopirate.appvsurveyapi.infrastructure.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataAccessException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    private val logger = LoggerFactory.getLogger(AuthServiceImpl::class.java)

    @Value("\${app.admin.username}")
    private lateinit var adminUsername: String

    @Value("\${app.admin.email}")
    private lateinit var adminEmail: String

    @Value("\${app.admin.password}")
    private lateinit var adminPassword: String

    fun createAdminUserIfNotExists() {
        try {
            if (!userRepository.existsByEmail(adminEmail)) {
                val adminUser = User(
                    username = adminUsername,
                    email = adminEmail,
                    password = passwordEncoder.encode(adminPassword),
                    role = "ADMIN"
                )
                userRepository.save(adminUser)
                logger.info("Admin user created successfully")
            } else {
                logger.debug("Admin user already exists")
            }
        } catch (e: DataAccessException) {
            logger.error("Failed to create admin user", e)
            throw IllegalStateException("Failed to initialize admin user", e)
        } catch (e: Exception) {
            logger.error("Unexpected error while creating admin user", e)
            throw IllegalStateException("Failed to initialize application", e)
        }
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = try {
            userRepository.findByEmail(request.email)
                .orElseThrow { BadCredentialsException("Invalid credentials") }
        } catch (e: DataAccessException) {
            logger.error("Database error during login for email: ${request.email}", e)
            throw IllegalStateException("Authentication service temporarily unavailable", e)
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid credentials")
        }

        // Clean up old refresh tokens for this user
        try {
            refreshTokenRepository.deleteByUser(user)
        } catch (e: DataAccessException) {
            logger.warn("Failed to clean up old refresh tokens for user: ${user.username}", e)
            // Continue with login process even if cleanup fails
        }

        // Generate new tokens
        val accessToken = try {
            jwtService.generateToken(user.username)
        } catch (e: Exception) {
            logger.error("Failed to generate access token for user: ${user.username}", e)
            throw IllegalStateException("Failed to generate authentication token", e)
        }

        val refreshToken = createRefreshToken(user)

        return LoginResponse(
            username = user.username,
            AccessToken = accessToken,
            RefreshToken = refreshToken.token,
            ExpiresIn = 86400000 // 24 hours in milliseconds
        )
    }

    fun refreshToken(refreshTokenString: String): LoginResponse {
        val refreshToken = try {
            refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow { BadCredentialsException("Invalid refresh token") }
        } catch (e: DataAccessException) {
            logger.error("Database error while validating refresh token", e)
            throw IllegalStateException("Authentication service temporarily unavailable", e)
        }

        if (refreshToken.expiryDate.isBefore(LocalDateTime.now())) {
            try {
                refreshTokenRepository.delete(refreshToken)
            } catch (e: DataAccessException) {
                logger.warn("Failed to delete expired refresh token", e)
            }
            throw BadCredentialsException("Refresh token expired")
        }

        val user = refreshToken.user
        val newAccessToken = try {
            jwtService.generateToken(user.username)
        } catch (e: Exception) {
            logger.error("Failed to generate new access token for user: ${user.username}", e)
            throw IllegalStateException("Failed to refresh authentication token", e)
        }

        return LoginResponse(
            username = user.username,
            AccessToken = newAccessToken,
            RefreshToken = refreshToken.token,
            ExpiresIn = 86400000
        )
    }

    private fun createRefreshToken(user: User): RefreshToken {
        val token = UUID.randomUUID().toString()
        val expiryDate = LocalDateTime.now().plusDays(7) // 7 days

        val refreshToken = RefreshToken(
            token = token,
            user = user,
            expiryDate = expiryDate
        )

        return try {
            refreshTokenRepository.save(refreshToken)
        } catch (e: DataAccessException) {
            logger.error("Failed to save refresh token for user: ${user.username}", e)
            throw IllegalStateException("Failed to create refresh token", e)
        }
    }


    fun cleanupExpiredTokens() {
        try {
            val deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now())
            logger.debug("Cleaned up {} expired refresh tokens ", deletedCount)
        } catch (e: DataAccessException) {
            logger.error("Failed to cleanup expired tokens", e)

        }
    }
}