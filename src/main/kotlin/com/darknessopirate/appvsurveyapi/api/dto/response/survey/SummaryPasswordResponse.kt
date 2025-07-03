package com.darknessopirate.appvsurveyapi.api.dto.response.survey

import java.time.LocalDateTime

data class SummaryPasswordResponse(
    val exists: Boolean,
    val password: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)