package com.darknessopirate.appvsurveyapi.api.dto.request

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.jetbrains.annotations.NotNull

@Schema(description = "Question create request dto")
data class QuestionRequest(
    @Schema(description = "Content of the question")
    @field:NotBlank(message = "Provide a question content")
    val text: String,
    @Schema(description = "Content of the question")
    val questionType: QuestionType,
    @Schema(description = "This will define whether the client will let user skip the question")
    val required: Boolean = false,
    @Schema(description = "The questions will be sorted according to their displayOrder values")
    val displayOrder: Int = 0,
    @Schema(description = "List of possible answers to choose from(leave list empty if question type is: OPEN)")
    @field:Valid
    val possibleAnswers: List<QuestionAnswerRequest>? = null
)