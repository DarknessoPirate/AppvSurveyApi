package com.darknessopirate.appvsurveyapi.api.dto.response.user

data class LoginResponse(
    val username: String,
    val AccessToken: String,
    val RefreshToken: String,
    val ExpiresIn: Long,
)