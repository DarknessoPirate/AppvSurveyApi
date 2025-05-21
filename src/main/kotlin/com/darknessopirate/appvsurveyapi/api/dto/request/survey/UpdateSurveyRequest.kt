package com.darknessopirate.appvsurveyapi.api.dto.request.survey

import java.time.LocalDateTime

data class UpdateSurveyRequest(
    val title: String? = null,
    val description: String? = null,
    val expiresAt: LocalDateTime? = null,
    val isActive: Boolean? = null
)