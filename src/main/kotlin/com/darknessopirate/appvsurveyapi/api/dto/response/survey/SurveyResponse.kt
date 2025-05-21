package com.darknessopirate.appvsurveyapi.api.dto.response.survey

import java.time.LocalDateTime

data class SurveyResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean,
    val accessCode: String?,
    val questionCount: Int
)