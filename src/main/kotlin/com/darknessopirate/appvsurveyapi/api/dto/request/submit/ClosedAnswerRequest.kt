package com.darknessopirate.appvsurveyapi.api.dto.request.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType

data class ClosedAnswerRequest(
    override val questionId: Long,
    val selectedAnswerIds: List<Long>,
    val selectionType: SelectionType
) : AnswerRequest(
    questionId,
    when(selectionType) {
        SelectionType.SINGLE -> QuestionType.DROPDOWN
        SelectionType.MULTIPLE -> QuestionType.CHECKBOX
    }
) {
    init {
        // Validate based on selection type
        if (selectionType == SelectionType.SINGLE && selectedAnswerIds.size > 1) {
            throw IllegalArgumentException("Single-selection question can have at most one selected answer")
        }
    }
}