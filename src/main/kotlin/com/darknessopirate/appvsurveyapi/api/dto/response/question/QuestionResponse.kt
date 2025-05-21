package com.darknessopirate.appvsurveyapi.api.dto.response.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType

sealed class QuestionResponse(
    val id: Long,
    val text: String,
    val required: Boolean,
    val displayOrder: Int,
    val isShared: Boolean,
    val type: QuestionType
)