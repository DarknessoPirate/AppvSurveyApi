package com.darknessopirate.appvsurveyapi.api.dto.response.question

data class QuestionCountsResponse(
    val sharedQuestions: Long,
    val openQuestions: Long,
    val closedQuestions: Long,
    val checkboxQuestions: Long,
    val dropdownQuestions: Long
)