package com.darknessopirate.appvsurveyapi.api.dto.response.survey

data class SurveyCountsResponse(
    val totalSurveys: Long,
    val activeSurveys: Long,
    val inactiveSurveys: Long,
    val expiredSurveys: Long,
    val expiringSurveys: Long
)