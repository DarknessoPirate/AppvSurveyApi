package com.darknessopirate.appvsurveyapi.api.dto.request.survey

import jakarta.validation.constraints.NotBlank

data class GetSummaryRequest(
    @field:NotBlank(message = "Password cannot be blank")
    val password: String
)