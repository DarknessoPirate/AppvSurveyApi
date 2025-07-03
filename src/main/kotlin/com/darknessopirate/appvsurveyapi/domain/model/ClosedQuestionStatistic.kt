package com.darknessopirate.appvsurveyapi.domain.model

data class ClosedQuestionStatistic(
    override val questionId: Long,
    override val questionText: String,
    override val totalResponses: Int,
    val selectionType: String,
    val answerOptions: List<AnswerStatistic>
) : QuestionStatistic(
    questionId = questionId,
    questionText = questionText,
    questionType = "CLOSED",
    totalResponses = totalResponses
)
