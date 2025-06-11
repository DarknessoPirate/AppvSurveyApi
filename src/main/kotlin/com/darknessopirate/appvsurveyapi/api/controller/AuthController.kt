package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.user.LoginRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.user.RefreshTokenRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.user.LoginResponse
import com.darknessopirate.appvsurveyapi.infrastructure.service.AuthServiceImpl
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthServiceImpl
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<LoginResponse> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }
}