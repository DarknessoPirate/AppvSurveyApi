package com.darknessopirate.appvsurveyapi.api.dto.request.survey

import java.time.LocalDate

data class UpdateSurveyRequest(
    val title: String,
    val description: String? = null,
    val expiresAt: LocalDate? = null,
)