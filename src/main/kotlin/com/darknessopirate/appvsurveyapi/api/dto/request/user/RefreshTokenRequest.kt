package com.darknessopirate.appvsurveyapi.api.dto.request.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class RefreshTokenRequest(
    @field:NotEmpty(message = "Refresh token cannot be empty")
    @field:NotBlank(message = "Refresh token cannot be blank")
    val refreshToken: String
)