package com.darknessopirate.appvsurveyapi.api.dto.response.survey

import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import java.time.LocalDateTime

data class SurveyDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean,
    val accessCode: String?,
    val questions: List<QuestionResponse>
)