package com.darknessopirate.appvsurveyapi.api.dto.response.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType

data class ClosedQuestionResponse(
    val questionId: Long,
    val questionText: String,
    val isRequired: Boolean,
    val order: Int,
    val shared: Boolean,
    val selectionType: SelectionType,
    val possibleAnswers: List<QuestionAnswerResponse>
) : QuestionResponse(
    questionId,
    questionText,
    isRequired,
    order,
    shared,
    when(selectionType) {
        SelectionType.SINGLE -> QuestionType.DROPDOWN
        SelectionType.MULTIPLE -> QuestionType.CHECKBOX
    }
)