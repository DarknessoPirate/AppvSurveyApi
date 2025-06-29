package com.darknessopirate.appvsurveyapi.api.dto.response.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

data class OpenQuestionResponse(
    override val id: Long,
    override val text: String,
    override val description: String?,
    override val required: Boolean,
    override val displayOrder: Int,
    override val isShared: Boolean,
) : QuestionResponse(id, text, description, required, displayOrder, isShared, QuestionType.OPEN)