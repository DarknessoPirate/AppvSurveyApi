package com.darknessopirate.appvsurveyapi.api.dto.response.submit

import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionAnswerResponse
import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType

data class ClosedUserAnswerResponse(
    override val questionId: Long,
    override val questionText: String,
    val selectedAnswers: List<QuestionAnswerResponse>,
    val selectionType: SelectionType
) : UserAnswerResponse(
    questionId,
    questionText,
    when(selectionType) {
        SelectionType.SINGLE -> QuestionType.DROPDOWN
        SelectionType.MULTIPLE -> QuestionType.CHECKBOX
    }
)