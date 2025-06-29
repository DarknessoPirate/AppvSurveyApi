package com.darknessopirate.appvsurveyapi.api.dto.request.question

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "questionType" // This property in JSON will determine the type
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OpenQuestionRequest::class, name = "OPEN"),
    JsonSubTypes.Type(value = ClosedQuestionRequest::class, name = "DROPDOWN"), // Note: DROPDOWN for SINGLE selection
    JsonSubTypes.Type(value = ClosedQuestionRequest::class, name = "CHECKBOX")  // Note: CHECKBOX for MULTIPLE selection
)
sealed class QuestionRequest(
    open val text: String,
    open val description: String?,
    open val required: Boolean = false,
    open val questionType: QuestionType
)