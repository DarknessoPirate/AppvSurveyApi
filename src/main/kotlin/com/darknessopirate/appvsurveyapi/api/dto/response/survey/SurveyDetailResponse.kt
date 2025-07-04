package com.darknessopirate.appvsurveyapi.api.dto.response.survey

import com.darknessopirate.appvsurveyapi.api.dto.response.accessCode.AccessCodeResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import java.time.LocalDate
import java.time.LocalDateTime

data class SurveyDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDate?,
    val isActive: Boolean,
    val accessCodes: List<AccessCodeResponse>,
    val questions: List<QuestionResponse>,
    val hasSummaryPassword: Boolean
)