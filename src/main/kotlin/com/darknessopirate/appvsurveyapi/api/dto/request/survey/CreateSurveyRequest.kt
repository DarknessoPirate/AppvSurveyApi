package com.darknessopirate.appvsurveyapi.api.dto.request.survey

import java.time.LocalDateTime

data class CreateSurveyRequest(
    val title: String,
    val description: String? = null,
    val expiresAt: LocalDateTime? = null,
)