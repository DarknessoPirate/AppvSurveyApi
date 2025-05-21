package com.darknessopirate.appvsurveyapi.api.dto.response.submit

data class SubmissionSummaryResponse(
    val surveyId: Long,
    val surveyTitle: String,
    val totalSubmissions: Int,
    val submissionsLast24Hours: Int,
    val submissionsLast7Days: Int,
    val averagePerDay: Double
)