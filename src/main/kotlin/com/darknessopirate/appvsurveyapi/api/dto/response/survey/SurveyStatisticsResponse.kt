package com.darknessopirate.appvsurveyapi.api.dto.response.survey

import com.darknessopirate.appvsurveyapi.domain.model.QuestionStatistic
import java.time.LocalDate
import java.time.LocalDateTime

data class SurveyStatisticsResponse(
    val surveyId: Long,
    val surveyTitle: String,
    val surveyDescription: String?,
    val totalSubmissions: Int,
    val questionStatistics: List<QuestionStatistic>,
)