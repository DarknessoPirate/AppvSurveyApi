package com.darknessopirate.appvsurveyapi.api.dto.response.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

data class OpenQuestionResponse(
    val questionId: Long,
    val questionText: String,
    val isRequired: Boolean,
    val order: Int,
    val shared: Boolean
) : QuestionResponse(questionId, questionText, isRequired, order, shared, QuestionType.OPEN)