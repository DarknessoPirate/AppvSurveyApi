package com.darknessopirate.appvsurveyapi.api.dto.response

data class UserAnswerResponse(
    val id: Long?,
    val questionId: Long,
    val textValue: String?,
    val selectedAnswerIds: List<Long>
)