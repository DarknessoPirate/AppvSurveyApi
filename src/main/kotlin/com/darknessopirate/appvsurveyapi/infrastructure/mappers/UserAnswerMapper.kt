package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.request.UserAnswerRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.UserAnswerResponse
import com.darknessopirate.appvsurveyapi.domain.model.Question
import com.darknessopirate.appvsurveyapi.domain.model.QuestionAnswer
import com.darknessopirate.appvsurveyapi.domain.model.UserAnswer
import org.springframework.stereotype.Component

@Component
class UserAnswerMapper {

    fun toEntity(request: UserAnswerRequest, question: Question, selectedAnswers: List<QuestionAnswer>): UserAnswer {
        return UserAnswer(
            question = question,
            textValue = request.textValue,
            selectedAnswers = selectedAnswers.toMutableList()
        )
    }

    fun toResponse(userAnswer: UserAnswer): UserAnswerResponse {
        return UserAnswerResponse(
            id = userAnswer.id,
            questionId = userAnswer.question.id!!,
            textValue = userAnswer.textValue,
            selectedAnswerIds = userAnswer.selectedAnswers.mapNotNull { it.id }
        )
    }

    fun toResponseList(userAnswers: List<UserAnswer>): List<UserAnswerResponse> {
        return userAnswers.map { toResponse(it) }
    }
}