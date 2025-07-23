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
            // Find all existing admin users
            val existingAdmins = userRepository.findByRole("ADMIN")

            // Check if current config admin already exists and is up to date
            val currentConfigAdmin = existingAdmins.find { it.email == adminEmail }

            if (currentConfigAdmin != null) {
                // Admin with current email exists, check if username and password match
                var needsUpdate = false

                if (currentConfigAdmin.username != adminUsername) {
                    currentConfigAdmin.username = adminUsername
                    needsUpdate = true
                    logger.info("Updating admin username to: $adminUsername")
                }

                if (!passwordEncoder.matches(adminPassword, currentConfigAdmin.password)) {
                    currentConfigAdmin.password = passwordEncoder.encode(adminPassword)
                    needsUpdate = true
                    logger.info("Updating admin password")
                }

                if (needsUpdate) {
                    userRepository.save(currentConfigAdmin)
                    logger.info("Admin user updated successfully")
                }

                // Clean up any other admin accounts (stale ones)
                val staleAdmins = existingAdmins.filter { it.id != currentConfigAdmin.id }
                if (staleAdmins.isNotEmpty()) {
                    cleanupStaleAdminAccounts(staleAdmins)
                }

            } else {
                // No admin with current email exists
                if (existingAdmins.isNotEmpty()) {
                    // There are admin accounts but with different emails (stale)
                    logger.info("Found ${existingAdmins.size} stale admin account(s), cleaning up...")
                    cleanupStaleAdminAccounts(existingAdmins)
                }

                // Create new admin user with current config
                val adminUser = User(
                    username = adminUsername,
                    email = adminEmail,
                    password = passwordEncoder.encode(adminPassword),
                    role = "ADMIN"
                )
                userRepository.save(adminUser)
                logger.info("New admin user created successfully with email: $adminEmail")
            }

        } catch (e: DataAccessException) {
            logger.error("Failed to create/update admin user", e)
            throw IllegalStateException("Failed to initialize admin user", e)
        } catch (e: Exception) {
            logger.error("Unexpected error while creating/updating admin user", e)
            throw IllegalStateException("Failed to initialize application", e)
        }
    }

    private fun cleanupStaleAdminAccounts(staleAdmins: List<User>) {
        try {
            staleAdmins.forEach { admin ->
                // First, clean up any refresh tokens for this admin
                try {
                    refreshTokenRepository.deleteByUser(admin)
                    logger.debug("Cleaned up refresh tokens for stale admin: ${admin.email}")
                } catch (e: DataAccessException) {
                    logger.warn("Failed to clean up refresh tokens for admin: ${admin.email}", e)
                }

                // Delete the admin user
                userRepository.delete(admin)
                logger.info("Deleted stale admin account: ${admin.email} (username: ${admin.username})")
            }
        } catch (e: DataAccessException) {
            logger.error("Failed to cleanup stale admin accounts", e)
            throw IllegalStateException("Failed to cleanup stale admin accounts", e)
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