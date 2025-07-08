package com.darknessopirate.appvsurveyapi.api.dto.request.accessCode

import java.time.LocalDateTime

data class CreateAccessCodeRequest(
    val title: String,
    val description: String? = null,
    val code: String? = null,
    val expiresAt: LocalDateTime? = null,
    val maxUses: Int? = null
)