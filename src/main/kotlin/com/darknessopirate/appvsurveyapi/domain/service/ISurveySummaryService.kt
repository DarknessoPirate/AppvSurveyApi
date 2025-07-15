package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SurveySummaryPassword

interface ISurveySummaryService {
    fun setSummaryPassword(surveyId: Long, password: String): SurveySummaryPassword
    fun getSummaryPassword(surveyId: Long): SurveySummaryPassword?
    fun passwordExists(surveyId: Long): Boolean
    fun getSurveyStatistics(surveyId: Long, password: String): SurveyStatisticsResponse
    fun generateSurveyStatistics(surveyId: Long): SurveyStatisticsResponse
    fun deleteSummaryPassword(surveyId: Long)
}