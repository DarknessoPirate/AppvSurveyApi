package com.darknessopirate.appvsurveyapi.api.dto.response.question

data class QuestionAnswerResponse(
    val id: Long,
    val text: String,
    val displayOrder: Int
)