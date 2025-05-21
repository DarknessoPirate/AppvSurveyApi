package com.darknessopirate.appvsurveyapi.api.dto.response.submit

import java.time.LocalDateTime

data class SubmissionDetailResponse(
    val id: Long,
    val surveyId: Long,
    val surveyTitle: String,
    val submittedAt: LocalDateTime,
    val answers: List<UserAnswerResponse>
)
