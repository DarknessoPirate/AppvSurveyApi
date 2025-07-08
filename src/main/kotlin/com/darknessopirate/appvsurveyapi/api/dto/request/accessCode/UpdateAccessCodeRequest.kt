package com.darknessopirate.appvsurveyapi.api.dto.request.accessCode

import java.time.LocalDateTime

data class UpdateAccessCodeRequest(
    val title: String,
    val code: String?,
    val description: String? = null,
    val isActive: Boolean,
    val expiresAt: LocalDateTime? = null,
    val maxUses: Int? = null
)
