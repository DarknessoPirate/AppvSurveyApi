package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.request.QuestionAnswerRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.QuestionAnswerResponse
import com.darknessopirate.appvsurveyapi.domain.model.QuestionAnswer
import org.springframework.stereotype.Component

@Component
class QuestionAnswerMapper {

    fun toEntity(request: QuestionAnswerRequest): QuestionAnswer {
        return QuestionAnswer(
            text = request.text,
            displayOrder = request.displayOrder
        )
    }

    fun toResponse(questionAnswer: QuestionAnswer): QuestionAnswerResponse {
        return QuestionAnswerResponse(
            id = questionAnswer.id,
            text = questionAnswer.text,
            displayOrder = questionAnswer.displayOrder
        )
    }

    fun toResponseList(questionAnswers: List<QuestionAnswer>): List<QuestionAnswerResponse> {
        return questionAnswers.map { toResponse(it) }
    }
}