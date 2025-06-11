package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.request.user.LoginRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.user.LoginResponse
import com.darknessopirate.appvsurveyapi.domain.entity.user.RefreshToken
import com.darknessopirate.appvsurveyapi.domain.entity.user.User
import com.darknessopirate.appvsurveyapi.domain.repository.user.RefreshTokenRepository
import com.darknessopirate.appvsurveyapi.infrastructure.repository.UserRepository
import com.darknessopirate.appvsurveyapi.infrastructure.security.JwtService
import org.springframework.beans.factory.annotation.Value
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

    @Value("\${app.admin.username}")
    private lateinit var adminUsername: String

    @Value("\${app.admin.email}")
    private lateinit var adminEmail: String

    @Value("\${app.admin.password}")
    private lateinit var adminPassword: String

    fun createAdminUserIfNotExists() {
        if (!userRepository.existsByEmail(adminEmail)) {
            val adminUser = User(
                username = adminUsername,
                email = adminEmail,
                password = passwordEncoder.encode(adminPassword),
                role = "ADMIN"
            )
            userRepository.save(adminUser)
        }
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { BadCredentialsException("Invalid credentials") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid credentials")
        }

        // Clean up old refresh tokens for this user
        refreshTokenRepository.deleteByUser(user)

        // Generate new tokens
        val accessToken = jwtService.generateToken(user.username)
        val refreshToken = createRefreshToken(user)

        return LoginResponse(
            username = user.username,
            AccessToken = accessToken,
            RefreshToken = refreshToken.token,
            ExpiresIn = 86400000 // 24 hours in milliseconds
        )
    }

    fun refreshToken(refreshTokenString: String): LoginResponse {
        val refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow { BadCredentialsException("Invalid refresh token") }

        if (refreshToken.expiryDate.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken)
            throw BadCredentialsException("Refresh token expired")
        }

        val user = refreshToken.user
        val newAccessToken = jwtService.generateToken(user.username)

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

        return refreshTokenRepository.save(refreshToken)
    }

    // TODO : unused remove later
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username).orElse(null)
    }

    fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now())
    }
}