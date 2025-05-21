package com.darknessopirate.appvsurveyapi.api.dto.response.survey

import java.time.LocalDateTime

data class SurveyStatisticsResponse(
    val surveyId: Long,
    val title: String,
    val totalSubmissions: Int,
    val questionCounts: Map<String, Int>,
    val isActive: Boolean,
    val expiresAt: LocalDateTime?,
    val createdAt: LocalDateTime
)