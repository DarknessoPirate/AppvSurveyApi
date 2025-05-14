package com.darknessopirate.appvsurveyapi.api.dto.response

import java.time.LocalDateTime

data class SurveyResponse(
    val id: Long?,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean,
    val accessCode: String?,
    val questions: List<QuestionResponse>? = null
)