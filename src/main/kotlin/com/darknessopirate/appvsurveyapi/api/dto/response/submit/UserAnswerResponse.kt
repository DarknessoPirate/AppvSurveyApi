package com.darknessopirate.appvsurveyapi.api.dto.response.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

sealed class UserAnswerResponse(
    val questionId: Long,
    val questionText: String,
    val type: QuestionType
)