package com.darknessopirate.appvsurveyapi.domain.model

data class SubmissionSummary(
    val surveyId: Long,
    val surveyTitle: String,
    val totalSubmissions: Int,
    val submissionsLast24Hours: Int,
    val submissionsLast7Days: Int,
    val averagePerDay: Double
)