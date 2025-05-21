package com.darknessopirate.appvsurveyapi.api.dto.request.survey

import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import java.time.LocalDateTime

data class CreateSurveyWithQuestionsRequest(
    val title: String,
    val description: String? = null,
    val expiresAt: LocalDateTime? = null,
    val accessCode: String? = null,
    val questions: List<QuestionRequest>
)