package com.darknessopirate.appvsurveyapi.api.dto.request.survey

import java.time.LocalDateTime

data class CreateSurveyWithQuestionsRequest(
    val title: String,
    val description: String? = null,
    val expiresAt: LocalDateTime? = null,
    val questionIds: List<Long>
)