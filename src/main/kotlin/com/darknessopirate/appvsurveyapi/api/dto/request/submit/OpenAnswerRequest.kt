package com.darknessopirate.appvsurveyapi.api.dto.request.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

data class OpenAnswerRequest(
    override val questionId: Long,
    val textValue: String
) : AnswerRequest(questionId, QuestionType.OPEN)