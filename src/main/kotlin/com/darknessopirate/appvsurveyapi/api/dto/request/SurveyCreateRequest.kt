package com.darknessopirate.appvsurveyapi.api.dto.request

import java.time.LocalDateTime


class SurveyCreateRequest (
    val title: String,
    val description: String? = null,
    val expiresAt: LocalDateTime? = null,
    val isActive: Boolean = false,
    val questions: List<QuestionRequest>? = null
)