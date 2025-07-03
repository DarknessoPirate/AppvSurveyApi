package com.darknessopirate.appvsurveyapi.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

abstract class QuestionStatistic(
    open val questionId: Long,
    open val questionText: String,
    open val questionType: String,
    open val totalResponses: Int
)
