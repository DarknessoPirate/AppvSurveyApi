package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.request.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.model.Question
import org.springframework.stereotype.Component

@Component
class QuestionMapper(private val questionAnswerMapper: QuestionAnswerMapper) {

    fun toEntity(request: QuestionRequest): Question {
        val question = Question(
            text = request.text,
            questionType = request.questionType,
            required = request.required,
            displayOrder = request.displayOrder
        )

        request.possibleAnswers?.forEach { answerRequest ->
            val answer = questionAnswerMapper.toEntity(answerRequest)
            question.addPossibleAnswer(answer)
        }

        return question
    }

    fun toResponse(question: Question): QuestionResponse {
        return QuestionResponse(
            id = question.id,
            text = question.text,
            questionType = question.questionType,
            required = question.required,
            displayOrder = question.displayOrder,
            possibleAnswers = question.possibleAnswers.map { questionAnswerMapper.toResponse(it) }
        )
    }

    fun toResponseList(questions: List<Question>): List<QuestionResponse> {
        return questions.map { toResponse(it) }
    }
}