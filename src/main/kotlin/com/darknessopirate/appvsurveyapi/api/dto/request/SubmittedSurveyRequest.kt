package com.darknessopirate.appvsurveyapi.api.dto.request

data class SubmittedSurveyRequest(
    val surveyId: Long,
    val userAnswers: List<UserAnswerRequest>
)