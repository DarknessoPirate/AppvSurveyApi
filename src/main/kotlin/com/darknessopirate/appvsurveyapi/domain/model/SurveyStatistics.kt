package com.darknessopirate.appvsurveyapi.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class SurveyStatistics(
    val surveyId: Long,
    val title: String,
    val totalSubmissions: Int,
    val questionCounts: Map<String, Int>, // question type -> count
    val isActive: Boolean,
    val expiresAt: LocalDate?,
    val createdAt: LocalDateTime
)
