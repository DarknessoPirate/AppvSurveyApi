package com.darknessopirate.appvsurveyapi.api.dto.response.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

data class OpenUserAnswerResponse(
    val id: Long,
    val questionName: String,
    val textValue: String
) : UserAnswerResponse(id, questionName, QuestionType.OPEN)