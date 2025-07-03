package com.darknessopirate.appvsurveyapi.domain.model

import java.time.LocalDateTime

data class OpenResponse(
    val submissionId: Long,
    val textValue: String,
    val submittedAt: LocalDateTime
)