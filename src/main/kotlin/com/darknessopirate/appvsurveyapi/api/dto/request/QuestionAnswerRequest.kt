package com.darknessopirate.appvsurveyapi.api.dto.request

data class QuestionAnswerRequest(
    val text: String,
    val displayOrder: Int = 0
)