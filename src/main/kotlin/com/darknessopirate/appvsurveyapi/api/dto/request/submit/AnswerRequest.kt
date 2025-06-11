package com.darknessopirate.appvsurveyapi.api.dto.request.submit

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "questionType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OpenAnswerRequest::class, name = "OPEN"),
    JsonSubTypes.Type(value = ClosedAnswerRequest::class, name = "DROPDOWN"),
    JsonSubTypes.Type(value = ClosedAnswerRequest::class, name = "CHECKBOX")
)
sealed class AnswerRequest(
    open val questionId: Long,
    open val questionType: QuestionType
)