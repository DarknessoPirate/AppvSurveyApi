package com.darknessopirate.appvsurveyapi.api.dto.request.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import jakarta.validation.constraints.NotBlank

data class OpenQuestionRequest(
    @field:NotBlank(message = "Question text is required")
    override val text: String,
    override val description: String?,
    override val required: Boolean = false
) : QuestionRequest(text, description,required, QuestionType.OPEN)