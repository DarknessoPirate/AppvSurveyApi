package com.darknessopirate.appvsurveyapi.api.dto.response

import java.time.LocalDateTime

data class SubmittedSurveyResponse(
    val id: Long?,
    val surveyId: Long,
    val submittedAt: LocalDateTime,
    val userAnswers: List<UserAnswerResponse>
)