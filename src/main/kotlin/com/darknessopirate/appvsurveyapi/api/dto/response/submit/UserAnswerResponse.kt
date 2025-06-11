package com.darknessopirate.appvsurveyapi.api.dto.response.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

open class UserAnswerResponse(
    open val questionId: Long,
    open val questionText: String,
    val type: QuestionType
)