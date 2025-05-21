package com.darknessopirate.appvsurveyapi.api.dto.request.survey

data class CopySurveyRequest(
    val newTitle: String,
    val includeAccessCode: Boolean = false
)