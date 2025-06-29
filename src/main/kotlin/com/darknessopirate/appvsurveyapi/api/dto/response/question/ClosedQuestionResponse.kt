package com.darknessopirate.appvsurveyapi.api.dto.response.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType

data class ClosedQuestionResponse(
    override val id: Long,
    override val text: String,
    override val description: String?,
    override val required: Boolean,
    override val displayOrder: Int,
    override val isShared: Boolean,
    val selectionType: SelectionType,
    val possibleAnswers: List<QuestionAnswerResponse>
) : QuestionResponse(
    id,
    text,
    description,
    required,
    displayOrder,
    isShared,
    when(selectionType) {
        SelectionType.SINGLE -> QuestionType.DROPDOWN
        SelectionType.MULTIPLE -> QuestionType.CHECKBOX
    }
)