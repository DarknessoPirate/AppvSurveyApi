package com.darknessopirate.appvsurveyapi.api.dto.response.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

data class OpenUserAnswerResponse(
    override val questionId: Long,
    override val questionText: String,
    val textValue: String
) : UserAnswerResponse(questionId, questionText, QuestionType.OPEN)