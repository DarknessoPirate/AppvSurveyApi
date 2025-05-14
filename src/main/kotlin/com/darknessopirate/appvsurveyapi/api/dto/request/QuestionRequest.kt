package com.darknessopirate.appvsurveyapi.api.dto.request

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

data class QuestionRequest(
    val text: String,
    val questionType: QuestionType,
    val required: Boolean = false,
    val displayOrder: Int = 0,
    val possibleAnswers: List<QuestionAnswerRequest>? = null
)