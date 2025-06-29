package com.darknessopirate.appvsurveyapi.api.dto.response.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

sealed class QuestionResponse(
    open val id: Long,
    open val text: String,
    open val description: String?,
    open val required: Boolean,
    open val displayOrder: Int,
    open val isShared: Boolean,
    open val type: QuestionType
)