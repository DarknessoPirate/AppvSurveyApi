package com.darknessopirate.appvsurveyapi.api.dto.request

data class UserAnswerRequest(
    val questionId: Long,
    val textValue: String? = null,
    val selectedAnswerIds: List<Long> = emptyList()
)