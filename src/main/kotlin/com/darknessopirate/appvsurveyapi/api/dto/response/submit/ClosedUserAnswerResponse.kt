package com.darknessopirate.appvsurveyapi.api.dto.response.submit

import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionAnswerResponse
import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType

data class ClosedUserAnswerResponse(
    val id: Long,
    val questionName: String,
    val selectedAnswers: List<QuestionAnswerResponse>,
    val selectionType: SelectionType
) : UserAnswerResponse(
    id,
    questionName,
    when(selectionType) {
        SelectionType.SINGLE -> QuestionType.DROPDOWN
        SelectionType.MULTIPLE -> QuestionType.CHECKBOX
    }
)