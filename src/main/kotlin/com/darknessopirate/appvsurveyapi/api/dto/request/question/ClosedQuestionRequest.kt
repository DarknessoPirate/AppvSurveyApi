package com.darknessopirate.appvsurveyapi.api.dto.request.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import jakarta.validation.constraints.NotBlank

data class ClosedQuestionRequest(
    @field:NotBlank(message = "Question text is required")
    override val text: String,
    override val required: Boolean = false,
    val selectionType: SelectionType,
    val possibleAnswers: List<String>
) : QuestionRequest(
    text,
    required,
    when(selectionType) {
        SelectionType.SINGLE -> QuestionType.DROPDOWN
        SelectionType.MULTIPLE -> QuestionType.CHECKBOX
    }
)