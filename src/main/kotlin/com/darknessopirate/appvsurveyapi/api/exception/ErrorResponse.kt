package com.darknessopirate.appvsurveyapi.api.exception

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: Long
)