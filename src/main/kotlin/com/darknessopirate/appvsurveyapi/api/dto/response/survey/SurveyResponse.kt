package com.darknessopirate.appvsurveyapi.api.dto.response.survey

import java.time.LocalDate
import java.time.LocalDateTime

data class SurveyResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDate?,
    val isActive: Boolean,
    val accessCodeCount: Int, // Changed from accessCode
    val questionCount: Int,
    val hasSummaryPassword: Boolean
)