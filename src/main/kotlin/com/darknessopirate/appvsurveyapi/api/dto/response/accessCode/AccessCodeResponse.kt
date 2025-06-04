package com.darknessopirate.appvsurveyapi.api.dto.response.accessCode

import java.time.LocalDateTime

data class AccessCodeResponse(
    val id: Long,
    val code: String,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean,
    val usageCount: Int,
    val maxUses: Int?
)