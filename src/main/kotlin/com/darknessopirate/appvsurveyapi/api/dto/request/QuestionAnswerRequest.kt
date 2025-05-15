package com.darknessopirate.appvsurveyapi.api.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class QuestionAnswerRequest(
    @field:NotBlank(message = "Answer can't be empty") // TODO: Fix this validation not working
    val text: String,
    @field:NotNull(message = "If you don't need a certain order use -1 for displayOrder")
    val displayOrder: Int = 0
)