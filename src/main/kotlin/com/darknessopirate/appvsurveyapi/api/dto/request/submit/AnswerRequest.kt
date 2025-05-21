package com.darknessopirate.appvsurveyapi.api.dto.request.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

sealed class AnswerRequest(
    open val questionId: Long,
    open val questionType: QuestionType
)