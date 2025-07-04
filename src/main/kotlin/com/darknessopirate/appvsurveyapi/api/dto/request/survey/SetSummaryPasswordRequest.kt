package com.darknessopirate.appvsurveyapi.api.dto.request.survey

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SetSummaryPasswordRequest(
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 3, max = 50, message = "Password must be between 3 and 50 characters")
    val password: String
)