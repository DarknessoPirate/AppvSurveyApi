package com.darknessopirate.appvsurveyapi.domain.model

data class OpenQuestionStatistic(
    override val questionId: Long,
    override val questionText: String,
    override val totalResponses: Int,
    val responses: List<OpenResponse>
) : QuestionStatistic(
    questionId = questionId,
    questionText = questionText,
    questionType = "OPEN",
    totalResponses = totalResponses
)
