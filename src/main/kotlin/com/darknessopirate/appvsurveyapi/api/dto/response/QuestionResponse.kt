package com.darknessopirate.appvsurveyapi.api.dto.response

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

data class QuestionResponse(
    val id: Long?,
    val text: String,
    val questionType: QuestionType,
    val required: Boolean,
    val displayOrder: Int,
    val possibleAnswers: List<QuestionAnswerResponse>? = null
)