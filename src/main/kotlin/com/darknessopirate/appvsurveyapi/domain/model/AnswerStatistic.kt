package com.darknessopirate.appvsurveyapi.domain.model

data class AnswerStatistic(
    val answerId: Long,
    val answerText: String,
    val voteCount: Int,
    val percentage: Double
)
